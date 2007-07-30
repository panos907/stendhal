/*
 * @(#) src/games/stendhal/client/spiite/Sprite.java
 *
 * $Id$
 */

package games.stendhal.client.sprite;

//
//

import java.awt.Graphics;

/**
 * A sprite to be displayed on the screen. Note that a sprite contains no state
 * information, i.e. its just the image and not the location. This allows us to
 * use a single sprite in lots of different places without having to store
 * multiple copies of the image.
 */
public interface Sprite {
	/**
	 * Copy the sprite.
	 *
	 * @return	A new copy of the sprite.
	 */
	public Sprite copy();


	/**
	 * Create a sub-region of this sprite.
	 * <strong>NOTE: This does not use caching.</strong>
	 *
	 * @param	x		The starting X coordinate.
	 * @param	y		The starting Y coordinate.
	 * @param	width		The region width.
	 * @param	height		The region height.
	 * @param	ref		The sprite reference.
	 *
	 * @return	A new sprite.
	 */
	public Sprite createRegion(final int x, final int y, final int width, final int height, final Object ref);


	/**
	 * Draw the sprite onto the graphics context provided
	 * 
	 * @param g
	 *            The graphics context on which to draw the sprite
	 * @param x
	 *            The x location at which to draw the sprite
	 * @param y
	 *            The y location at which to draw the sprite
	 */
	public void draw(Graphics g, int x, int y);

	/**
	 * Draws the image
	 * 
	 * @param g
	 *            the graphics context where to draw to
	 * @param destx
	 *            destination x
	 * @param desty
	 *            destination y
	 * @param x
	 *            the source x
	 * @param y
	 *            the source y
	 * @param w
	 *            the width
	 * @param h
	 *            the height
	 */
	public void draw(Graphics g, int destx, int desty, int x, int y, int w, int h);

	/**
	 * Get the height of the drawn sprite
	 * 
	 * @return The height in pixels of this sprite
	 */
	public int getHeight();


	/**
	 * Get the sprite reference. This identifier is an externally
	 * opaque object that implements equals() and hashCode() to
	 * uniquely/repeatably reference a keyed sprite.
	 *
	 * @return	The reference identifier, or <code>null</code> if
	 *		not referencable.
	 */
	public Object getReference();


	/**
	 * Get the width of the drawn sprite
	 * 
	 * @return The width in pixels of this sprite
	 */
	public int getWidth();
}
