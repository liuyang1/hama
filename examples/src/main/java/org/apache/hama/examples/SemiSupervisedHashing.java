/**
 * Semi-Supervised Hashing.
 * liuyang1@mail.ustc.edu.cn
 */

package org.apache.hama.examples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Writable;
import org.apache.hama.HamaConfiguration;
import org.apache.hama.bsp.BSP;
import org.apache.hama.bsp.BSPJob;
import org.apache.hama.bsp.BSPJobClient;
import org.apache.hama.bsp.BSPPeer;
import org.apache.hama.bsp.ClusterStatus;
import org.apache.hama.bsp.FileOutputFormat;
import org.apache.hama.bsp.SequenceFileInputFormat;
import org.apache.hama.bsp.SequenceFileOutputFormat;
import org.apache.hama.bsp.sync.SyncException;
import org.apache.hama.commons.io.DenseVectorWritable;
import org.apache.hama.commons.io.SparseVectorWritable;
import org.apache.hama.commons.util.KeyValuePair;

public class SemiSupervisedHashing {
    protected static final Log LOG = LogFactory.getLog(SemiSupervisedHashing.class);
    // input and output param

    // input and output param function

    private static void startTask(HamaConfiguration conf)
        throws IOException, InterruptedException, ClassNotFoundException
    {
        BSPJob bsp = BSPJob(conf, SemiSupervisedHashing.class);
        bsp.setJobName("Semi-Supervised Hashing algorithm");
        bsp.setBspClass();
    }

    private static void parseArgs(HamaConfiguration conf, String[] args)
    {
        // TODO
    }

    public static void main(String[] args)
        throws IOException, InterruptedException, ClassNotFoundException
    {
        HamaConfiguration conf = new HamaConfiguration();
        parseArgs(conf, args);
        startTask(conf);
    }

    private static class SemiSupervisedHashingBSP
        extends BSP<??,??>
    {

        @Override
        public void setup(BSPPeer<??,??> peer)
            throws IOException, SyncException, InterruptedException
        {
            HamaConfiguration conf = (HamaConfiguration) peer.getConfiguration();
            // TODO
            peer.sync();
        }

        @Override
        public void bsp(BSPPeer<??,??> peer)
            throws IOException, SyncException, InterruptedException
        {
            // TODO
        }

        @Override
        public void cleanup(BSPPeer<??,??> peer)
            throws IOException, SyncException, InterruptedException
        {
            // TODO
        }
    }
}
