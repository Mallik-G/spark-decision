package de.kp.spark.decision.model
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

import org.json4s._

import org.json4s.native.Serialization
import org.json4s.native.Serialization.{read,write}

case class Listener(
  timeout:Int, url:String
)
/**
 * ServiceRequest & ServiceResponse specify the content 
 * sent to and received from the decision service
 */
case class ServiceRequest(
  service:String,task:String,data:Map[String,String]
)
case class ServiceResponse(
  service:String,task:String,data:Map[String,String],status:String
)
/*
 * The Field and Fields classes are used to specify the fields with
 * respect to the data source provided
 */
case class Field(
  name:String,datatype:String,value:String
)
case class Fields(items:List[Field])

/*
 * Service requests are mapped onto job descriptions and are stored
 * in a Redis instance
 */
case class JobDesc(
  service:String,task:String,status:String
)

case class Instance(label: String, features: Array[String]) {

  override def toString: String = {
    "Observation(%s, %s)".format(label, features.mkString("[", ", ", "]"))
  }

}

object Serializer {
    
  implicit val formats = Serialization.formats(NoTypeHints)
  
  def serializeFields(fields:Fields):String = write(fields)
  
  def deserializeFields(fields:String):Fields = read[Fields](fields)

  /*
   * Support for serialization and deserialization of job descriptions
   */
  def serializeJob(job:JobDesc):String = write(job)

  def deserializeJob(job:String):JobDesc = read[JobDesc](job)
  
  /*
   * Support for serialization of a service response and deserialization
   * of a certain serice request
   */
  def serializeResponse(response:ServiceResponse):String = write(response)
  
  def deserializeRequest(request:String):ServiceRequest = read[ServiceRequest](request)
  
}
object Algorithms {
  /* The value of the algorithms actually supported */
  val RF:String = "RF"
}

object Sources {
  /* The names of the data source actually supported */
  val FILE:String    = "FILE"
  val ELASTIC:String = "ELASTIC" 
  val JDBC:String    = "JDBC"    
}

object Messages {

  def ALGORITHM_IS_UNKNOWN(uid:String,algorithm:String):String = String.format("""Algorithm '%s' is unknown for uid '%s'.""", algorithm, uid)
 
  def DATA_TO_TRACK_RECEIVED(uid:String):String = String.format("""Data to track received for uid '%s'.""", uid)

  def GENERAL_ERROR(uid:String):String = String.format("""A general error appeared for uid '%s'.""", uid)
  
  def MISSING_FEATURES(uid:String):String = String.format("""Features are missing for uid '%s'.""", uid)

  def MISSING_PARAMETERS(uid:String):String = String.format("""Parameters are missing for uid '%s'.""", uid)

  def MODEL_BUILDING_STARTED(uid:String) = String.format("""Top-K Association Rule Mining started for uid '%s'.""", uid)

  def MODEL_DOES_NOT_EXIST(uid:String):String = String.format("""Model does not exist for uid '%s'.""", uid)
 
  def NO_ALGORITHM_PROVIDED(uid:String):String = String.format("""No algorithm provided for uid '%s'.""", uid)

  def NO_PARAMETERS_PROVIDED(uid:String):String = String.format("""No parameters provided for uid '%s'.""", uid)

  def NO_SOURCE_PROVIDED(uid:String):String = String.format("""No source provided for uid '%s'.""", uid)

  def REQUEST_IS_UNKNOWN():String = String.format("""Unknown request.""")

  def SEARCH_INDEX_CREATED(uid:String):String = String.format("""Search index created for uid '%s'.""", uid)
 
  def SOURCE_IS_UNKNOWN(uid:String,source:String):String = String.format("""Source '%s' is unknown for uid '%s'.""", source, uid)

  def TASK_ALREADY_STARTED(uid:String):String = String.format("""The task with uid '%s' is already started.""", uid)

  def TASK_DOES_NOT_EXIST(uid:String):String = String.format("""The task with uid '%s' does not exist.""", uid)

  def TASK_IS_UNKNOWN(uid:String,task:String):String = String.format("""The task '%s' is unknown for uid '%s'.""", task, uid)
  
}

object DecisionStatus {
  
  val DATASET:String = "dataset"
    
  val STARTED:String = "started"
  val FINISHED:String = "finished"
  
  val FAILURE:String = "failure"
  val SUCCESS:String = "success"
    
}