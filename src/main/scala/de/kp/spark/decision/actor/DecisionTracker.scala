package de.kp.spark.decision.actor
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

import java.util.Date

import de.kp.spark.decision.model._
import de.kp.spark.decision.io.{ElasticBuilderFactory => EBF,ElasticWriter}

import scala.collection.JavaConversions._
import scala.collection.mutable.HashMap

class DecisionTracker extends BaseActor {
  
  def receive = {
    
    case req:ServiceRequest => {

      val uid = req.data("uid")
      
      val data = Map("uid" -> uid, "message" -> Messages.DATA_TO_TRACK_RECEIVED(uid))
      val response = new ServiceResponse(req.service,req.task,data,DecisionStatus.SUCCESS)	
      
      val origin = sender
      origin ! Serializer.serializeResponse(response)

      try {

        val index   = req.data("index")
        val mapping = req.data("type")

        val writer = new ElasticWriter()
    
        val readyToWrite = writer.open(index,mapping)
        if (readyToWrite == false) {
      
          writer.close()
      
          val msg = String.format("""Opening index '%s' and mapping '%s' for write failed.""",index,mapping)
          throw new Exception(msg)
      
        } else {
      
          /* Prepare data */
          val source = prepare(req.data)
          /*
           * Writing this source to the respective index throws an
           * exception in case of an error; note, that the writer is
           * automatically closed 
           */
          writer.write(index, mapping, source)
        
        }
      
      } catch {
        
        case e:Exception => {
          log.error(e, e.getMessage())
        }
      
      } finally {
        
        context.stop(self)

      }
    }
    
  }
  
  private def prepare(params:Map[String,String]):java.util.Map[String,Object] = {
    
    val now = new Date()
    val source = HashMap.empty[String,String]
    
    source += EBF.SITE_FIELD -> params(EBF.SITE_FIELD)
    source += EBF.TIMESTAMP_FIELD -> now.getTime().toString    
 
    /* 
     * Restrict parameters to those that are relevant to feature description;
     * note, that we use a flat JSON data structure for simplicity and distinguish
     * field semantics by different prefixes 
     */
    val records = params.filter(kv => kv._1.startsWith("lbl.") || kv._1.startsWith("fea."))
    for (rec <- records) {
      
      val (k,v) = rec
        
      val name = k.replace("lbl.","").replace("fea.","")
      source += k -> v      
      
    }

    source
    
  }

}