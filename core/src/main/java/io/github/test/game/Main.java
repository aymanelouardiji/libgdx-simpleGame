package io.github.test.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms.
 */
public class Main extends ApplicationAdapter {
    Texture backgroundTexture;
    Texture bucketTexture;
    Texture dropTexture;
    Sound dropSound;
    Vector2 touchPos;
    float dropTimer;
    SpriteBatch spriteBatch;
    Array<Sprite> dropSprites;
    Rectangle bucketRectangle;
    Rectangle dropRectangle;
    FitViewport viewport;
    Sprite bucketSprite;
    Music music;
    int score = 0;
    boolean isPaused = false;
    BitmapFont font;
    Texture pauseTexture, resumeTexture;
    Rectangle pauseButtonBounds, resumeButtonBounds;


    @Override
    public void create() {
        backgroundTexture = new Texture("background.png");
        bucketTexture = new Texture("bucket.png");
        dropTexture = new Texture("drop.png");
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.mp3"));
        music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
        spriteBatch = new SpriteBatch();
        bucketSprite = new Sprite(bucketTexture); // Initialize the sprite based on the texture
        bucketSprite.setSize(1, 1); // Define the size of the sprite
        touchPos = new Vector2();
        dropSprites = new Array<>();
        viewport = new FitViewport(8, 5); spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8, 5);
        bucketRectangle = new Rectangle();
        dropRectangle = new Rectangle();

        pauseTexture = new Texture("play.png");
        resumeTexture = new Texture("pause.png");

        pauseButtonBounds = new Rectangle(7f, 4.5f, 0.5f, 0.5f); // top-right corner
        resumeButtonBounds = new Rectangle(6.4f, 4.5f, 0.5f, 0.5f); // next to pause

        music.setLooping(true);
        music.setVolume(.5f);
        music.play();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true); // true centers the camera
    }


    @Override
    public void render() {
        input();
        if (!isPaused) {
            logic();
        }
        draw();
    }


    private void input() {
        float speed = 4f;
        float delta = Gdx.graphics.getDeltaTime(); // retrieve the current delta

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            bucketSprite.translateX(speed * delta); // Move the bucket right
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            bucketSprite.translateX(-speed * delta); // move the bucket left
        }

        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touchPos);

            // Handle pause/resume buttons
            if (pauseButtonBounds.contains(touchPos)) {
                isPaused = true;
                music.pause(); // Pause music
            } else if (resumeButtonBounds.contains(touchPos)) {
                isPaused = false;
                music.play(); // Resume music
            }


            bucketSprite.setCenterX(touchPos.x); // Change the horizontally centered position of the bucket
        }
        // 🔁 Reset game when 'R' is pressed
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            score = 0;
            dropSprites.clear();
        }
    }


    private void logic() {
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        // Store the bucket size for brevity
        float bucketWidth = bucketSprite.getWidth();
        float bucketHeight = bucketSprite.getHeight();

        // Subtract the bucket width
        bucketSprite.setX(MathUtils.clamp(bucketSprite.getX(), 0, worldWidth - bucketWidth));

        float delta = Gdx.graphics.getDeltaTime(); // retrieve the current delta
        bucketRectangle.set(bucketSprite.getX(), bucketSprite.getY(), bucketWidth, bucketHeight);

        // loop through each drop
        for (int i = dropSprites.size - 1; i >= 0; i--) {
            Sprite dropSprite = dropSprites.get(i); // Get the sprite from the list

            float dropWidth = dropSprite.getWidth();
            float dropHeight = dropSprite.getHeight();

            dropSprite.translateY(-2f * delta); // move the drop downward every frame

            // Apply the drop position and size to the dropRectangle
            dropRectangle.set(dropSprite.getX(), dropSprite.getY(), dropWidth, dropHeight);

            if (dropSprite.getY() < -dropHeight) dropSprites.removeIndex(i);
            else if (bucketRectangle.overlaps(dropRectangle)) {
                dropSprites.removeIndex(i); // Remove the drop
                dropSound.play(); // Play the sound
                score++; // Increment score
            }
        }
        dropTimer += delta; // Adds the current delta to the timer
        if (dropTimer > 1f) { // Check if it has been more than a second
            dropTimer = 0; // Reset the timer
            createDroplet(); // Create the droplet
        }
    }
    private void createDroplet() {
        // create local variables for convenience
        float dropWidth = 1;
        float dropHeight = 1;
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        // create the drop sprite
        Sprite dropSprite = new Sprite(dropTexture);
        dropSprite.setSize(dropWidth, dropHeight);
        dropSprite.setX(MathUtils.random(0f, worldWidth - dropWidth)); // Randomize the drop's x position
        dropSprite.setY(worldHeight);
        dropSprites.add(dropSprite); // Add it to the list
    }


    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        spriteBatch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);
        bucketSprite.draw(spriteBatch);
        for (Sprite dropSprite : dropSprites) {
            dropSprite.draw(spriteBatch);
        }

        if (!isPaused) {
            spriteBatch.draw(pauseTexture, pauseButtonBounds.x, pauseButtonBounds.y, pauseButtonBounds.width, pauseButtonBounds.height);
        } else {
            spriteBatch.draw(resumeTexture, resumeButtonBounds.x, resumeButtonBounds.y, resumeButtonBounds.width, resumeButtonBounds.height);
        }

        spriteBatch.end();
    }


}
