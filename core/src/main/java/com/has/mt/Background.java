package com.has.mt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.List;

public class Background {

    private List<Texture> layers = new ArrayList<>();
    private List<Float> factors = new ArrayList<>();

    public Background(String folderName) {
        setBackground(folderName);
    }

    public void setBackground(String folderName) {
        // Dispose existing textures
        for (Texture t : layers) {
            t.dispose();
        }
        layers.clear();
        factors.clear();

        // Load new layers
        FileHandle dir = Gdx.files.internal("Backgrounds/Winter/Layered/" + folderName);
        FileHandle[] files = dir.list(".png");
        List<FileHandle> pngFiles = new ArrayList<>();
        for (FileHandle f : files) {
            if (!f.name().equals("hd.png")) {
                pngFiles.add(f);
            }
        }
        // Sort by name
        pngFiles.sort((f1, f2) -> f1.name().compareTo(f2.name()));
        // Load textures and assign factors
        for (int i = 0; i < pngFiles.size(); i++) {
            Texture texture = new Texture(pngFiles.get(i));
            texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.ClampToEdge);
            layers.add(texture);
            float factor = 0.1f * (i + 1);
            factors.add(factor);
        }
    }

    public void draw(SpriteBatch batch, float cameraX, float alpha) {
        batch.setColor(1, 1, 1, alpha);
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        for (int i = 0; i < layers.size(); i++) {
            Texture texture = layers.get(i);
            float factor = factors.get(i);
            float textureWidth = texture.getWidth();
            float u = (cameraX * factor) / textureWidth;
            float u2 = u + (screenWidth / textureWidth);
            batch.draw(texture, 0, 0, screenWidth, screenHeight, u, 0, u2, 1);
        }
        batch.setColor(1, 1, 1, 1);
    }

    public void dispose() {
        for (Texture t : layers) {
            t.dispose();
        }
    }
}
