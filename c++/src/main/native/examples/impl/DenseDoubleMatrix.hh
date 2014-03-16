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

#include <string>

using std::string;

namespace math {
  
  class DenseDoubleMatrix {
  private:
    double **matrix;
    int row;
    int col;
  public:
    // Default-Constructor
    DenseDoubleMatrix();
    DenseDoubleMatrix(int row, int col);
    // Create a new matrix with the given size and default value.
    // DenseDoubleMatrix(int row, int col, double val);

    // Creates a new vector with the given array.
    DenseDoubleMatrix(int row, int col, double data[][]);
    DenseDoubleMatrix(const string values);
    ~DenseDoubleVector();  // Destructor

    int getRow();
    int getCol();
    int getDimension(); //??
    void set(int rindex,int cindex, double value);
    double get(int rindex,int cindex);

    DenseDoubleMatrix *add(DenseDoubleMatrix *v);
    // DenseDoubleMatrix *add(double scalar);
    // DenseDoubleMatrix *subtract(DenseDoubleMatrix *v);
    // DenseDoubleMatrix *subtract(double v);
    // DenseDoubleMatrix *subtractFrom(double v);

    DenseDoubleMatrix *multiply(double scalar);
    // DenseDoubleMatrix *divide(double scalar);
    // double sum();

    // DenseDoubleVector *abs();
    // double dot(DenseDoubleMatrix *s);

    // double max();
    // int maxIndex();
    // double min();
    // int minIndex();

    // double *toArray();
    virtual string toString();
  };
}
