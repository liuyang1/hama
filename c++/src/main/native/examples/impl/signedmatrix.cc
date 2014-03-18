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
/*
 * liuyang1 
 * 2014-03-18 08:49:27 framework
 */

#include "hama/Pipes.hh"
#include "hama/TemplateFactory.hh"
#include "hadoop/StringUtils.hh"
#include "DenseDoubleVector.hh"

#include <time.h>
#include <math.h>

using std::string;
using std::vector;

using HamaPipes::BSP;
using HamaPipes::BSPJob;
using HamaPipes::Partitioner;
using HamaPipes::BSPContext;

using math::DenseDoubleVector;

#define SIGNEDMATRIXDIM     960
class SignedMatrixBSP: public BSP<int, string, int, string, string>
{
    private:
        // TODO
        string mMasterTask;
        int mSeqFileID;
        string HAMA_MAT_MULT_B_PATH;
        vector<DenseDoubleVector*> matrix;

    public:
        SignedMatrixBSP(BSPContext<int, string, int, string, string>& context)
        {
            mSeqFileID = 0;
            HAMA_MAT_MULT_B_PATH = "hama.mat.mult.B.path";
        }

        void setup(BSPContext<int, string, int, string, string>& context)
        {
            mMasterTask = context.getPeerName(context.getNumPeers() / 2);

            reopenMatrix(context);
            int col_key;
            string col_vec_str;
            while(context.sequenceFileReadNext<int, string>(mSeqFileID, col_key, col_vec_str))
            {
                DenseDoubleVector* col_vec = new DenseDoubleVector(col_vec_str);
                matrix.push_back(col_vec);
            }
        }

        void bsp(BSPContext<int, string, int, string, string>& context)
        {
            int row_key = 0;
            string row_vec_str;

            double ans[SIGNEDMATRIXDIM][SIGNEDMATRIXDIM];
            for(int i=0; i< SIGNEDMATRIXDIM; i++)
                for(int j=0; j< SIGNEDMATRIXDIM; j++)
                    ans[i][j] = 0;
            while(context.readNext(row_key, row_vec_str))
            {
                DenseDoubleVector *row_vec = new DenseDoubleVector(row_vec_str);
                double* row = row_vec->toArray();
                int flaglen = row_vec->getLength() - SIGNEDMATRIXDIM;
                if(flaglen <= 0)
                {
                    continue;
                }
                int* flag = new int[flaglen];
                for(int i = 0; i < flaglen; i++)
                {
                    flag[i] = int(row[SIGNEDMATRIXDIM+i]);
                    DenseDoubleVector *col = matrix[flag[i]];
                    for(int j = 0; j < SIGNEDMATRIXDIM; j++)
                    {
                        double v = row[j];
                        for(int k = 0; k < SIGNEDMATRIXDIM; k++)
                        {
                            ans[j][k] += v*col->get(k);
                        }
                    }
                }
            }
        }

        void reopenMatrix(BSPContext<int, string, int, string, string>& context)
        {
            if (mSeqFileID!=0){
                context.sequenceFileClose(mSeqFileID);
            }
            const BSPJob* job = context.getBSPJob();
            string path = job->get(HAMA_MAT_MULT_B_PATH);
            mSeqFileID = context.sequenceFileOpen(path, "r",
                    "org.apache.hadoop.io.IntWritable",
                    "org.apache.aham.commons.io.PipesVectorWritable");
        }

};

class SignedPartitioner: public Partitioner<int, string>
{
    public:
        SignedPartitioner(BSPContext<int, string, int, string, string>& context)
        {
        }

        int partition(const int& key, const string& value, int32_t num_tasks)
        {
            return key % num_tasks;
        }
};

int main(int argc, char **argv)
{
    return HamaPipes::runTask<int, string, int, string, string>(HamaPipes::TemplateFactory<SignedMatrixBSP, int, string, int, string, string, SignedPartitioner>());
}
