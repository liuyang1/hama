/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import org.apache.hadoop.io.Text;
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
    private static final String inputPathString = "ssh.inputpath";
    private static final String outputPathString = "ssh.outputpath";
    // input and output param

    // input and output param function
    public static class SemiSupervisedHashingBSP
        extends BSP<NullWritable, NullWritable, Text, DoubleWritable, DoubleWritable>
    {

        @Override
        public void setup(BSPPeer<NullWritable, NullWritable, Text, DoubleWritable, DoubleWritable> peer)
            throws IOException, SyncException, InterruptedException
        {
            // HamaConfiguration conf = (HamaConfiguration) peer.getConfiguration();
            // TODO
            // peer.sync();
        }

        @Override
        public void bsp(BSPPeer<NullWritable, NullWritable, Text, DoubleWritable, DoubleWritable> peer)
            throws IOException, SyncException, InterruptedException
        {
            // TODO
        }

        @Override
        public void cleanup(BSPPeer<NullWritable, NullWritable, Text, DoubleWritable, DoubleWritable> peer)
            throws IOException// only IOException
        {
            // TODO
        }
    }

    private static void printUsage() {
        LOG.info("Usage: ssh <input> <output>");
    }

    private static void startTask(HamaConfiguration conf)
        throws IOException, InterruptedException, ClassNotFoundException
    {
        BSPJob bsp = new BSPJob(conf, SemiSupervisedHashing.class);
        bsp.setJobName("Semi-Supervised Hashing algorithm");
        // bsp.setBspClass(
    }

    private static void parseArgs(HamaConfiguration conf, String[] args)
    {
        if (args.length < 2) {
            printUsage();
            System.exit(-1);
        }
        // conf.set(inputPathString, args[0]);

        // Path path = new Path(args[1]);
        // conf.set(outputPathString, path.toString());
        // TODO
    }

    public static void main(String[] args)
        throws IOException, InterruptedException, ClassNotFoundException
    {
        HamaConfiguration conf = new HamaConfiguration();
        parseArgs(conf, args);
        startTask(conf);
    }

}
