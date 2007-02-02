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
package games.stendhal.client;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;

import javax.imageio.ImageIO;

import marauroa.common.Log4J;

import org.apache.log4j.Logger;

/**
 * A resource manager for sprites in the game. Its often quite important how and
 * where you get your game resources from. In most cases it makes sense to have
 * a central resource loader that goes away, gets your resources and caches them
 * for future use.
 * <p>
 * [singleton]
 * <p>
 * 
 * @author Kevin Glass
 */
public class SpriteStore {
	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(SpriteStore.class);

	/** The single instance of this class */
	private static SpriteStore single = new SpriteStore();
	
	private static boolean doOldBootstrapClassloaderWorkaroundFirst = true;

	protected SpriteStore() {
	}

	/**
	 * Get the single instance of this class
	 * 
	 * @return The single instance of this class
	 */
	public static SpriteStore get() {
		return single;
	}

	/** The cached sprite map, from reference to sprite instance */
	private HashMap<String, Sprite> sprites = new HashMap<String, Sprite>();

	/**
	 * Retrieve a collection of sprites from the store.
	 * 
	 * @param ref
	 *            the sprite name
	 * @param animation
	 *            the position of the animation starting in 0.
	 * @param width
	 *            of the frame
	 * @param height
	 *            of the frame
	 */
	public Sprite[] getAnimatedSprite(String ref, int animation, int frames,
			double width, double height) {
		return getAnimatedSprite(getSprite(ref), animation, frames, width,
				height);
	}

	/**
	 * @param animImage The image which contains the different animation frames
	 * @param animation The vertical position of this animation's frames inside
	 *                  the image  
	 * @param frameCount The number of frames in this animation
	 * @param width The width of one animation frame, in tiles
	 * @param height The height of one animation frame, in tiles
	 * @return
	 */
	public Sprite[] getAnimatedSprite(Sprite animImage, int animation,
			int frameCount, double width, double height) {
		// calculate width and height in pixels from width and height
		// in tiles
		int pixelWidth = (int) (width * GameScreen.SIZE_UNIT_PIXELS);
		int pixelHeight = (int) (height * GameScreen.SIZE_UNIT_PIXELS);

		Sprite[] animatedSprite = new Sprite[frameCount];

		GraphicsConfiguration gc = GraphicsEnvironment
				.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getDefaultConfiguration();

		for (int i = 0; i < frameCount; i++) {
			Image image = gc.createCompatibleImage(pixelWidth, pixelHeight,
					Transparency.BITMASK);
			// Bugfixs: parameters width and height added, see comment in
			// Sprite.java
			// animImage.draw(image.getGraphics(),0,0,i*iwidth,animation*iheight);
			// intensifly @ gmx.com, April 20th, 2006
			animImage.draw(image.getGraphics(), 0, 0, i * pixelWidth, animation
					* pixelHeight, pixelWidth, pixelHeight);
			animatedSprite[i] = new Sprite(image);
		}

		return animatedSprite;
	}

	public void free(String ref) {
		sprites.put(ref, null);
		sprites.remove(ref);
	}

	/**
	 * Retrieve a sprite from the store
	 * 
	 * @param ref
	 *            The reference to the image to use for the sprite
	 * @return A sprite instance containing an accelerate image of the request
	 *         reference
	 */
	public Sprite getSprite(String ref) {
		return getSprite(ref, false);
	}

	public Sprite getSprite(String ref, boolean loadAlpha) {
		// if we've already got the sprite in the cache
		// then just return the existing version
		if (sprites.get(ref) != null) {
			return sprites.get(ref);
		}

		// otherwise, go away and grab the sprite from the resource
		// loader
		BufferedImage sourceImage = null;

		try {
			URL url = getResourceURL(ref);
			if (url == null) {
				logger.fatal("Can't find ref: " + ref);
				return getSprite("data/sprites/failsafe.png");
			}

			// use ImageIO to read the image in
			sourceImage = ImageIO.read(url);
		} catch (IOException e) {
			e.printStackTrace();
			logger.fatal("Failed to load: " + ref);
			return getSprite("data/sprites/failsafe.png");
		}

		// create an accelerated image of the right size to store our sprite in
		GraphicsConfiguration gc = GraphicsEnvironment
				.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getDefaultConfiguration();

		int mode = Transparency.BITMASK;

		// ALPHA channel makes it runs 30% slower.
		// if(loadAlpha)
		// {
		// mode=Transparency.TRANSLUCENT;
		// }

		Image image = gc.createCompatibleImage(sourceImage.getWidth(),
				sourceImage.getHeight(), mode);

		// draw our source image into the accelerated image
		image.getGraphics().drawImage(sourceImage, 0, 0, null);

		// create a sprite, add it the cache then return it
		Sprite sprite = new Sprite(image);
		sprites.put(ref, sprite);

		return sprite;
	}

	/**
	 * gets a resource URL. Use this method instead of classLoader.getResouce()
	 * because there are still clients around with a broken classloader
	 * prefering old resources. This method ensures we get the sprite
	 * from the appropriate place, this helps with deploying the game
	 * with things like webstart and updates. 
	 *
	 * @param ref name of resource
	 * @return URL to this resouce
	 */
	public URL getResourceURL(String ref) {
		return doOldBootstrapClassloaderWorkaround(ref);
	}

	/**
	 * Warning, ugly workaround for a bug in Bootstrap.java prior (including) version 0.57.
	 *
	 * @param ref resource name
	 * @return URL to that resource or null in case it was not found
	 */
	private URL doOldBootstrapClassloaderWorkaround(String ref) {
		URL url = null;
		try {
			ClassLoader classloader = this.getClass().getClassLoader();
			Method method = ClassLoader.class.getDeclaredMethod("findResource", String.class);
			method.setAccessible(true);
			
			url = (URL) method.invoke(classloader, ref);
			if (url == null) {
				ClassLoader parent = classloader.getParent();
				if (parent != null) {
					url = parent.getResource(ref);
				}
			}
		} catch (Exception e) {
			if (doOldBootstrapClassloaderWorkaroundFirst) {
				logger.error(e, e);
				e.printStackTrace(System.err);
				doOldBootstrapClassloaderWorkaroundFirst = false;
			}
		}
		if (url == null) {
			url = this.getClass().getClassLoader().getResource(ref);
		}
		return url;
	}
	}