/* $Id$ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server;

import marauroa.common.*;
import marauroa.common.net.*;
import marauroa.common.game.*;
import marauroa.server.game.*;

import games.stendhal.common.*;

import java.util.*;
import java.io.*;

public class StendhalRPRuleProcessor implements IRPRuleProcessor
  {
  private RPServerManager rpman; 
  private RPWorld world;
  
  private LinkedList<RPObject> playersObject;
  
  public StendhalRPRuleProcessor() throws FileNotFoundException, IOException 
    {
    playersObject=new LinkedList<RPObject>();
    }


  /**
   * Set the context where the actions are executed.
   *
   * @param rpman
   * @param world
   */
  public void setContext(RPServerManager rpman, RPWorld world)
    {
    try
      {
      this.rpman=rpman;
      this.world=world;
      
      StendhalRPAction.initialize(rpman,world);
      }
    catch(Exception e)
      {
      Logger.thrown("StendhalRPRuleProcessor::setContext","!",e);
      System.exit(-1);
      }
    }

  public void approvedActions(RPObject.ID id, List<RPAction> actionList)
    {
    }

  public RPAction.Status execute(RPObject.ID id, RPAction action)
    {
    Logger.trace("StendhalRPRuleProcessor::execute",">");

    RPAction.Status status=RPAction.Status.FAIL;

    try
      {
      if(action.get("type").equals("move"))
        {
        Logger.trace("StendhalRPRuleProcessor::execute","D","Got Move action: "+action.toString());
        RPObject object=world.get(id);
        if(action.has("dx")) object.put("dx",action.getDouble("dx"));
        if(action.has("dy")) object.put("dy",action.getDouble("dy"));
        world.modify(object);
        }
      else if(action.get("type").equals("change"))
        {
        RPObject object=world.get(id);
        if(action.has("dest") && !object.get("zoneid").equals(action.get("dest")))
          {
          StendhalRPAction.changeZone(object,action.get("dest"));
          StendhalRPAction.transferContent(object);
          }
        }
      else if(action.get("type").equals("chat"))
        {
        if(action.has("text")) 
          {
          RPObject object=world.get(id);
          object.put("text",action.get("text"));
          world.modify(object);
          }
        }          
      else if(action.get("type").equals("face"))
        {
        if(action.has("dir")) 
          {
          RPObject object=world.get(id);
          object.put("dir",action.get("dir"));
          world.modify(object);
          }
        }          
      }
    catch(Exception e)
      {
      Logger.trace("StendhalRPRuleProcessor::execute","X",action.toString());
      Logger.thrown("StendhalRPRuleProcessor::execute","X",e);
      }
    finally
      {
      Logger.trace("StendhalRPRuleProcessor::execute","<");
      }

    return status;
    }
  
  /** Notify it when a new turn happens */
  synchronized public void nextTurn()
    {
    Logger.trace("StendhalRPRuleProcessor::nextTurn",">");
    try
      {
      for(RPObject object: playersObject)
        {
        StendhalRPAction.move(object);
        }
      }
    catch(Exception e)
      {
      Logger.thrown("StendhalRPRuleProcessor::nextTurn","X",e);
      }    
      
    Logger.trace("StendhalRPRuleProcessor::nextTurn","<");
    }

  synchronized public boolean onInit(RPObject object) throws RPObjectInvalidException
    {
    Logger.trace("StendhalRPRuleProcessor::onInit",">");
    try
      {
      object.put("zoneid","village");
      object.put("x",30+Math.random()*10-5);
      object.put("y",30+Math.random()*10-5);
      object.put("dx",0);
      object.put("dy",0);
      world.add(object);
      
      StendhalRPAction.transferContent(object);
      
      playersObject.add(object);
      return true;
      }
    catch(Exception e)
      {
      Logger.thrown("StendhalRPRuleProcessor::onInit","X",e);
      return false;
      }        
    finally
      {
      Logger.trace("StendhalRPRuleProcessor::onInit","<");
      }
    }

  synchronized public boolean onExit(RPObject.ID id)
    {
    Logger.trace("StendhalRPRuleProcessor::onExit",">");
    try
      {
      for(RPObject object: playersObject)
        {
        if(object.getID().equals(id))
          {
          boolean result=playersObject.remove(object);
          Logger.trace("StendhalRPRuleProcessor::onExit","D","Removed Player was "+result);
          break;
          }
        }
      
      world.remove(id);

      return true;
      }
    catch(Exception e)
      {
      Logger.thrown("StendhalRPRuleProcessor::onExit","X",e);
      return true;
      }
    finally
      {
      Logger.trace("StendhalRPRuleProcessor::onExit","<");
      }
    }

  synchronized public boolean onTimeout(RPObject.ID id)
    {
    Logger.trace("StendhalRPRuleProcessor::onTimeout",">");
    try
      {
      for(RPObject object: playersObject)
        {
        if(object.getID().equals(id))
          {
          playersObject.remove(object);
          break;
          }
        }

      world.remove(id);
        
      return true;
      }
    catch(Exception e)
      {
      Logger.thrown("StendhalRPRuleProcessor::onTimeout","X",e);
      return true;
      }
    finally
      {
      Logger.trace("StendhalRPRuleProcessor::onTimeout","<");
      }
    }
  }


