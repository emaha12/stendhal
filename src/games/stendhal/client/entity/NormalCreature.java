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
package games.stendhal.client.entity;

import games.stendhal.client.GameObjects;
import games.stendhal.client.Sprite;
import games.stendhal.client.SpriteStore;
import marauroa.common.game.AttributeNotFoundException;
import marauroa.common.game.RPObject;



/** A Creature entity */
public class NormalCreature extends Creature 
  {
  public NormalCreature(GameObjects gameObjects, RPObject object) throws AttributeNotFoundException
    {
    super(gameObjects, object);
    }
  
  protected void buildAnimations(RPObject object)
    {
    SpriteStore store=SpriteStore.get();  
    Sprite creature=loadAnimationSprite(object);

    sprites.put("move_up", store.getAnimatedSprite(creature,0,4,1.5,2));      
    sprites.put("move_right", store.getAnimatedSprite(creature,1,4,1.5,2));      
    sprites.put("move_down", store.getAnimatedSprite(creature,2,4,1.5,2));      
    sprites.put("move_left", store.getAnimatedSprite(creature,3,4,1.5,2));      

    sprites.get("move_up")[3]=sprites.get("move_up")[1];
    sprites.get("move_right")[3]=sprites.get("move_right")[1];
    sprites.get("move_down")[3]=sprites.get("move_down")[1];
    sprites.get("move_left")[3]=sprites.get("move_left")[1];
    }
  
  protected Sprite defaultAnimation()
    {
    animation="move_up";
    return sprites.get("move_up")[0];
    }
  }
