/*
 * Copyright (C) 2018 Stefano Peris
 * eMail: <xenon77.dev@gmail.com>
 * Github: https://github.com/XenonCoder
 */

package com.xenonlab.skybox2d;

import java.awt.Color;
import java.awt.Graphics2D;


public abstract class Entity extends GameObject {
	
	//CONSTANTS
	private final float GRAVITY = 0.2F;
	private final float MAX_FALLING_SPEED = 2.5F;
	private final float JUMP_START = -3.0F;
	
	//MOVEMENT
	protected float dx;
	protected float dy;
	protected float speed;
	
	protected boolean right;
	protected boolean left;
	protected boolean falling;
	protected boolean hasFallen;
	protected boolean jumping;
	
	//COLLISION
	protected boolean topLeft;
	protected boolean topRight;
	protected boolean bottomLeft;
	protected boolean bottomRight;
	protected boolean midLeft;
	protected boolean midRight;
	protected boolean hasfallen;
	
	//ANIMATION
	private int idle;
	private final int IDLE_LEFT = 0;
	private final int IDLE_RIGHT = 1;
	private final int LEFT = 2;
	private final int RIGHT = 3;
	private final int[] frames = {1, 1, 2, 2};
	protected Spritesheet sprite;
	protected Animation animation;
	
	public Entity(Spritesheet sprite, float x, float y, int width, int height, float speed) {
		super(x, y, width, height);
		this.speed = speed;
		this.sprite = sprite;
		this.idle = IDLE_LEFT;
		this.animation = new Animation(sprite, 150L, LEFT, frames[LEFT]);
	}
	
	public void update() {
		calculateMovement(); //1
		calculateCollisions(); //2
		calculateAnimations();
		move(); //3
	}
	
	public void render(Graphics2D g) {
		g.setColor(Color.RED);
		g.fillRect((int)x, (int)y, width, height);
		g.drawImage(animation.getImage(), (int)x, (int)y, null);
	}
	
	private void calculateCollisions() {
		World world = Playstate.world;
		float tox = x + dx;
		float toy = y + dy;
		
		calculateCorners(tox, y - 1);
		if(dx < 0) {
			if(topLeft || bottomLeft || midLeft) {
				dx = 0;
			}
		}
		
		if(dx > 0) {
			if(topRight || bottomRight || midRight) {
				dx = 0;
			}
		}
		
		calculateCorners(x, toy);
		if(topLeft || topRight) {
			dy = 0;
			falling = true;
			int playerrow = Playstate.world.getRowTile((int)toy);
			y = (playerrow + 1) * Main.BLOCKSIZE;
		}
		
		if(bottomLeft || bottomRight && falling) {
			falling = false;
			int playerRow = world.getRowTile((int)toy + height);
			y = (playerRow * Main.BLOCKSIZE - height);
			dy = 0;
		} 
		
		if(!bottomLeft && !bottomRight) {
			hasFallen = true;
			falling = true;
		}
	}
	
	
	private void calculateCorners(float x, float y) {
		World world = Playstate.world;
		int leftTile = world.getColTile((int)x);
		int rightTile = world.getColTile((int)x + width - 1);
		int topTile = world.getRowTile((int)y);
		int bottomTile = world.getRowTile((int)y + height);
		int midTile = world.getRowTile((int)y + height / 2);
		topLeft = !world.getBlocks()[topTile][leftTile].getMaterial().isWalkable();
		bottomLeft = !world.getBlocks()[bottomTile][leftTile].getMaterial().isWalkable();
		topRight = !world.getBlocks()[topTile][rightTile].getMaterial().isWalkable();
		bottomRight = !world.getBlocks()[bottomTile][rightTile].getMaterial().isWalkable();
		midLeft = !world.getBlocks()[midTile][leftTile].getMaterial().isWalkable();
		midRight = !world.getBlocks()[midTile][rightTile].getMaterial().isWalkable();
	}
	
	private void calculateMovement() {
		if(left) dx = -speed;
		if(right) dx = speed;
		
		if(falling && !jumping) {
			dy += GRAVITY;
			if(dy > MAX_FALLING_SPEED) dy = MAX_FALLING_SPEED;
		}
		
		if(jumping && !falling) {
			dy = JUMP_START;
			jumping = false;
			falling = true;
		}
	}
	
	private void calculateAnimations() {
		animation.update();
		if(left && animation.getState() != LEFT) {
			animation.setImages(LEFT, frames[LEFT]);
			idle = IDLE_LEFT;
		} else if(right && animation.getState() != RIGHT) {
			animation.setImages(RIGHT, frames[RIGHT]);
			idle = IDLE_RIGHT;
		}  
		
		if(!left && !right) {
			animation.setImages(idle, frames[idle]);
		}
	}
	
	private void move() {
		x += dx;
		y += dy;
		dx = 0;
	}
}
