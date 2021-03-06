package de.kp.spark.decision.tree
/* Copyright (c) 2014 Dr. Krusche & Partner PartG
 * 
 * This file is part of the Spark-Decision project
 * (https://github.com/skrusche63/spark-decision).
 * 
 * Spark-Decision is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Spark-Decision is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with
 * Spark-Decision. 
 * 
 * If not, see <http://www.gnu.org/licenses/>.
 */

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

import org.apache.spark.mllib.linalg.{Vectors, Vector}
import org.apache.spark.mllib.regression.LabeledPoint

object DTHandler {
  
  def format(dataset:RDD[(Long,Long,String,Double)]):(RDD[LabeledPoint],Map[Int,Int],Long) = {
    
    /* Group & sort by columns */
    val trainset = dataset.groupBy(x => x._1).sortBy(x => x._1).map(x => {
      
      val columns = x._2.map{case(row,col,category,value) => (col,value)}.toSeq.sortBy(_._1)
      /*
       * The label is expected to be the last column entry
       */
      val features = Vectors.dense(columns.init.map(_._2).toArray)
      val label = columns.last._2
      
      LabeledPoint(label,features)
      
    })
    /*
     * Determine the categorical features and derive the respective 
     * categorical info; this is done by first restricting the dataset
     * to 'categorical' entries, second by grouping with respect to 'col',
     * and third, by determining the highest categorical value
     */
    val categorical_info = dataset.filter{case(row,col,category,value) => category == "categorical"}
          .groupBy(x => x._2).map(x => {
            /*
             * A map storing information about the categorical variables and the
             * number of discrete values they take. 
             * 
             * For example, an entry (n -> k) implies the feature n is categorical 
             * with k categories 0, 1, 2, ... , k-1. 
             * 
             * It's important to note that features are zero-indexed.
             */
            val n = x._1.toInt
            val k = x._2.map(_._4).max.toInt + 1 
          
            (n,k)
            
          }).collect.toMap

    /*
     * The number of different classes for multiclass classification
     * is determined from the number of distinct labels; note, that a 
     * label is of value 0, 1, 2, etc
     */
    val num_classes = trainset.map(_.label).distinct.count
    (trainset,categorical_info,num_classes)
    
  }

}