package org.apache.hama.mapred;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Scanner;
import org.apache.hadoop.hbase.filter.RowFilterInterface;
import org.apache.hadoop.hbase.filter.RowFilterSet;
import org.apache.hadoop.hbase.filter.StopRowFilter;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.io.RowResult;
import org.apache.hadoop.hbase.mapred.TableSplit;
import org.apache.hadoop.hbase.regionserver.HRegion;
import org.apache.hadoop.hbase.util.Writables;
import org.apache.hadoop.mapred.InputFormat;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hama.AbstractBase;
import org.apache.hama.Vector;
import org.apache.hama.io.VectorWritable;

public abstract class MatrixInputFormatBase extends AbstractBase
implements InputFormat<ImmutableBytesWritable, Vector> {
  private final Log LOG = LogFactory.getLog(MatrixInputFormatBase.class);
  private byte [][] inputColumns;
  private HTable table;
  private TableRecordReader tableRecordReader;
  private RowFilterInterface rowFilter;

  /**
   * Iterate over an HBase table data, return (Text, VectorResult) pairs
   */
  protected class TableRecordReader
  implements RecordReader<ImmutableBytesWritable, Vector> {
    private byte [] startRow;
    private byte [] endRow;
    private RowFilterInterface trrRowFilter;
    private Scanner scanner;
    private HTable htable;
    private byte [][] trrInputColumns;

    /**
     * Build the scanner. Not done in constructor to allow for extension.
     *
     * @throws IOException
     */
    public void init() throws IOException {
      if ((endRow != null) && (endRow.length > 0)) {
        if (trrRowFilter != null) {
          final Set<RowFilterInterface> rowFiltersSet =
            new HashSet<RowFilterInterface>();
          rowFiltersSet.add(new StopRowFilter(endRow));
          rowFiltersSet.add(trrRowFilter);
          this.scanner = this.htable.getScanner(trrInputColumns, startRow,
            new RowFilterSet(RowFilterSet.Operator.MUST_PASS_ALL,
              rowFiltersSet));
        } else {
          this.scanner =
            this.htable.getScanner(trrInputColumns, startRow, endRow);
        }
      } else {
        this.scanner =
          this.htable.getScanner(trrInputColumns, startRow, trrRowFilter);
      }
    }

    /**
     * @param htable the {@link HTable} to scan.
     */
    public void setHTable(HTable htable) {
      this.htable = htable;
    }

    /**
     * @param inputColumns the columns to be placed in {@link VectorWritable}.
     */
    public void setInputColumns(final byte [][] inputColumns) {
      this.trrInputColumns = inputColumns;
    }

    /**
     * @param startRow the first row in the split
     */
    public void setStartRow(final byte [] startRow) {
      this.startRow = startRow;
    }

    /**
     *
     * @param endRow the last row in the split
     */
    public void setEndRow(final byte [] endRow) {
      this.endRow = endRow;
    }

    /**
     * @param rowFilter the {@link RowFilterInterface} to be used.
     */
    public void setRowFilter(RowFilterInterface rowFilter) {
      this.trrRowFilter = rowFilter;
    }

    /** {@inheritDoc} */
    public void close() throws IOException {
      this.scanner.close();
    }

    /**
     * @return ImmutableBytesWritable
     *
     * @see org.apache.hadoop.mapred.RecordReader#createKey()
     */
    public ImmutableBytesWritable createKey() {
      return new ImmutableBytesWritable();
    }

    /**
     * @return VectorResult
     *
     * @see org.apache.hadoop.mapred.RecordReader#createValue()
     */
    public Vector createValue() {
      return new Vector();
    }

    /** {@inheritDoc} */
    public long getPos() {
      // This should be the ordinal tuple in the range;
      // not clear how to calculate...
      return 0;
    }

    /** {@inheritDoc} */
    public float getProgress() {
      // Depends on the total number of tuples and getPos
      return 0;
    }

    /**
     * @param key HStoreKey as input key.
     * @param value MapWritable as input value
     *
     * Converts Scanner.next() to Text, VectorResult
     *
     * @return true if there was more data
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public boolean next(ImmutableBytesWritable key, Vector value)
    throws IOException {
      RowResult result = this.scanner.next();
      boolean hasMore = result != null && result.size() > 0;
      if (hasMore) {
        key.set(result.getRow());
        Writables.copyWritable(result, value);
      }
      return hasMore;
    }
  }

  /**
   * Builds a TableRecordReader. If no TableRecordReader was provided, uses
   * the default.
   *
   * @see org.apache.hadoop.mapred.InputFormat#getRecordReader(InputSplit,
   *      JobConf, Reporter)
   */
  public RecordReader<ImmutableBytesWritable, Vector> getRecordReader(InputSplit split,
      @SuppressWarnings("unused")
      JobConf job, @SuppressWarnings("unused")
      Reporter reporter)
  throws IOException {
    TableSplit tSplit = (TableSplit) split;
    TableRecordReader trr = this.tableRecordReader;
    // if no table record reader was provided use default
    if (trr == null) {
      trr = new TableRecordReader();
    }
    trr.setStartRow(tSplit.getStartRow());
    trr.setEndRow(tSplit.getEndRow());
    trr.setHTable(this.table);
    trr.setInputColumns(this.inputColumns);
    trr.setRowFilter(this.rowFilter);
    trr.init();
    return trr;
  }

  /**
   * Calculates the splits that will serve as input for the map tasks.
   * <ul>
   * Splits are created in number equal to the smallest between numSplits and
   * the number of {@link HRegion}s in the table. If the number of splits is
   * smaller than the number of {@link HRegion}s then splits are spanned across
   * multiple {@link HRegion}s and are grouped the most evenly possible. In the
   * case splits are uneven the bigger splits are placed first in the
   * {@link InputSplit} array.
   *
   * @param job the map task {@link JobConf}
   * @param numSplits a hint to calculate the number of splits
   *
   * @return the input splits
   *
   * @see org.apache.hadoop.mapred.InputFormat#getSplits(org.apache.hadoop.mapred.JobConf, int)
   */
  public InputSplit[] getSplits(JobConf job, int numSplits) throws IOException {
    byte [][] startKeys = this.table.getStartKeys();
    if (startKeys == null || startKeys.length == 0) {
      throw new IOException("Expecting at least one region");
    }
    if (this.table == null) {
      throw new IOException("No table was provided");
    }
    if (this.inputColumns == null || this.inputColumns.length == 0) {
      throw new IOException("Expecting at least one column");
    }
    int realNumSplits = numSplits > startKeys.length ? startKeys.length
        : numSplits;
    InputSplit[] splits = new InputSplit[realNumSplits];
    int middle = startKeys.length / realNumSplits;
    int startPos = 0;
    for (int i = 0; i < realNumSplits; i++) {
      int lastPos = startPos + middle;
      lastPos = startKeys.length % realNumSplits > i ? lastPos + 1 : lastPos;
      splits[i] = new TableSplit(this.table.getTableName(),
          startKeys[startPos], ((i + 1) < realNumSplits) ? startKeys[lastPos]
              : HConstants.EMPTY_START_ROW);
      if (LOG.isDebugEnabled()) {
        LOG.debug("split: " + i + "->" + splits[i]);
      }
      startPos = lastPos;
    }
    return splits;

  }

  /**
   * @param inputColumns to be passed in {@link VectorWritable} to the map task.
   */
  protected void setInputColums(byte [][] inputColumns) {
    this.inputColumns = inputColumns;
  }

  /**
   * Allows subclasses to set the {@link HTable}.
   *
   * @param table to get the data from
   */
  protected void setHTable(HTable table) {
    this.table = table;
  }

  /**
   * Allows subclasses to set the {@link TableRecordReader}.
   *
   * @param tableRecordReader
   *                to provide other {@link TableRecordReader} implementations.
   */
  protected void setTableRecordReader(TableRecordReader tableRecordReader) {
    this.tableRecordReader = tableRecordReader;
  }

  /**
   * Allows subclasses to set the {@link RowFilterInterface} to be used.
   *
   * @param rowFilter
   */
  protected void setRowFilter(RowFilterInterface rowFilter) {
    this.rowFilter = rowFilter;
  }
}