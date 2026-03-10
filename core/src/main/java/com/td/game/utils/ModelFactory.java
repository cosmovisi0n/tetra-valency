package com.td.game.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.CylinderShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.SphereShapeBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.UBJsonReader;
import com.td.game.map.TileType;
import com.td.game.pillars.PillarType;

public class ModelFactory implements Disposable {
    private final ModelBuilder modelBuilder;
    private final long attributes;

    public ModelFactory() {
        modelBuilder = new ModelBuilder();
        attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
    }

    public Model createTileModel(TileType type) {
        Color tileColor;
        switch (type) {
            case DESERT_GROUND:
                tileColor = new Color(0.82f, 0.58f, 0.24f, 1f);
                break;
            case DESERT_PATH:
                tileColor = new Color(0.54f, 0.37f, 0.17f, 1f);
                break;
            case PATH:
                tileColor = new Color(0.55f, 0.45f, 0.3f, 1f);
                break;
            case GRASS:
            default:
                tileColor = new Color(0.15f, 0.35f, 0.12f, 1f);
                break;
        }
        Material material = new Material(ColorAttribute.createDiffuse(tileColor));
        return modelBuilder.createBox(
                Constants.TILE_SIZE * 0.95f,
                0.2f,
                Constants.TILE_SIZE * 0.95f,
                material,
                attributes);
    }

    public Model createHighlightModel(Color color) {
        Material material = new Material(
                ColorAttribute.createDiffuse(color),
                new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.5f));
        return modelBuilder.createBox(
                Constants.TILE_SIZE * 0.95f,
                0.1f,
                Constants.TILE_SIZE * 0.95f,
                material,
                attributes);
    }

    public Model createOrbModel(Color color) {
        Material material = new Material(
                ColorAttribute.createDiffuse(color),
                ColorAttribute.createSpecular(Color.WHITE));
        return modelBuilder.createSphere(0.8f, 0.8f, 0.8f, 20, 20, material, attributes);
    }

    private Model createFrustumPillar(Color pillarColor, Color orbColor) {
        modelBuilder.begin();
        float baseSize = 1.2f;
        float topSize = 0.6f;
        float height = 2.0f;

        Material material = new Material(ColorAttribute.createDiffuse(pillarColor));
        MeshPartBuilder builder = modelBuilder.part("frustum", GL20.GL_TRIANGLES, attributes, material);
        int segments = 16;
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * 2 * Math.PI / segments);
            float angle2 = (float) ((i + 1) * 2 * Math.PI / segments);
            float bx1 = (float) Math.cos(angle1) * baseSize / 2;
            float bz1 = (float) Math.sin(angle1) * baseSize / 2;
            float bx2 = (float) Math.cos(angle2) * baseSize / 2;
            float bz2 = (float) Math.sin(angle2) * baseSize / 2;
            float tx1 = (float) Math.cos(angle1) * topSize / 2;
            float tz1 = (float) Math.sin(angle1) * topSize / 2;
            float tx2 = (float) Math.cos(angle2) * topSize / 2;
            float tz2 = (float) Math.sin(angle2) * topSize / 2;
            builder.triangle(
                    new Vector3(bx1, 0, bz1),
                    new Vector3(tx1, height, tz1),
                    new Vector3(bx2, 0, bz2));
            builder.triangle(
                    new Vector3(bx2, 0, bz2),
                    new Vector3(tx1, height, tz1),
                    new Vector3(tx2, height, tz2));
        }

        Material topMat = new Material(ColorAttribute.createDiffuse(pillarColor.cpy().mul(1.2f)));
        MeshPartBuilder top = modelBuilder.part("top", GL20.GL_TRIANGLES, attributes, topMat);
        top.setVertexTransform(new com.badlogic.gdx.math.Matrix4().translate(0, height, 0));
        CylinderShapeBuilder.build(top, topSize, 0.1f, topSize, segments);

        if (orbColor != null) {
            Material orbMat = new Material(
                    ColorAttribute.createDiffuse(orbColor),
                    ColorAttribute.createSpecular(Color.WHITE));
            MeshPartBuilder orbBuilder = modelBuilder.part("orb", GL20.GL_TRIANGLES, attributes, orbMat);
            orbBuilder.setVertexTransform(
                    new com.badlogic.gdx.math.Matrix4()
                            .translate(0, height + 0.8f, 0)
                            .scale(1.5f, 1.5f, 1.5f));
            SphereShapeBuilder.build(orbBuilder, 0.5f, 0.5f, 0.5f, 20, 20);
        }

        return modelBuilder.end();
    }

    public Model createPillarModel(PillarType type, Color orbColor) {
        Color color;
        switch (type) {
            case RAPID:
                color = new Color(0.6f, 0.5f, 0.7f, 1f);
                break;
            case POWER:
                color = new Color(0.15f, 0.1f, 0.2f, 1f);
                break;
            case SNIPER:
                color = new Color(0.5f, 0.8f, 1f, 1f);
                break;
            default:
                color = new Color(0.5f, 0.5f, 0.5f, 1f);
        }
        return createFrustumPillar(color, orbColor);
    }

    public Model createPillarModel(PillarType type) {
        return createPillarModel(type, null);
    }

    public Model createPlayerModel() {
        return loadModel("3dmodels/Alchemist.g3db");
    }

    public Model createGateModel() {
        return loadModel("3dmodels/GATE.g3db");
    }

    public Model createCoreSphereModel(Color color) {
        return loadModel("3dmodels/CORE.g3db");
    }

    public Model loadPinkBlobModel() {
        // Pink blob - simple sphere
        Material material = new Material(ColorAttribute.createDiffuse(Color.PINK));
        return modelBuilder.createSphere(0.4f, 0.6f, 0.4f, 10, 10, material, attributes);
    }

    public Model loadGolemModel() {
        // Golem - box-based character
        modelBuilder.begin();
        Material bodyMaterial = new Material(ColorAttribute.createDiffuse(Color.GRAY));
        modelBuilder.part("body", GL20.GL_TRIANGLES, attributes, bodyMaterial).box(0.8f, 1f, 0.6f);
        Material headMaterial = new Material(ColorAttribute.createDiffuse(new Color(0.6f, 0.6f, 0.6f, 1f)));
        MeshPartBuilder head = modelBuilder.part("head", GL20.GL_TRIANGLES, attributes, headMaterial);
        head.setVertexTransform(new com.badlogic.gdx.math.Matrix4().translate(0, 0.8f, 0));
        head.box(0.5f, 0.4f, 0.4f);
        return modelBuilder.end();
    }

    public Model loadBatModel() {
        // Bat - dark sphere (flying enemy)
        Material material = new Material(ColorAttribute.createDiffuse(new Color(0.2f, 0.2f, 0.25f, 1f)));
        return modelBuilder.createSphere(0.4f, 0.6f, 0.4f, 10, 10, material, attributes);
    }

    public Model loadDemonModel() {
        // Demon boss - larger box with wings
        modelBuilder.begin();
        Material bodyMaterial = new Material(ColorAttribute.createDiffuse(Color.RED));
        modelBuilder.part("body", GL20.GL_TRIANGLES, attributes, bodyMaterial).box(0.6f, 0.3f, 1f);
        Material wingMaterial = new Material(ColorAttribute.createDiffuse(new Color(0.8f, 0.2f, 0.2f, 1f)));
        MeshPartBuilder wing1 = modelBuilder.part("wing1", GL20.GL_TRIANGLES, attributes, wingMaterial);
        wing1.setVertexTransform(new com.badlogic.gdx.math.Matrix4().translate(0.6f, 0.1f, 0).rotate(0, 0, 1, 15));
        wing1.box(0.8f, 0.05f, 0.4f);
        MeshPartBuilder wing2 = modelBuilder.part("wing2", GL20.GL_TRIANGLES, attributes, wingMaterial);
        wing2.setVertexTransform(new com.badlogic.gdx.math.Matrix4().translate(-0.6f, 0.1f, 0).rotate(0, 0, 1, -15));
        wing2.box(0.8f, 0.05f, 0.4f);
        return modelBuilder.end();
    }

    private FileHandle resolveAsset(String name) {
        FileHandle direct = Gdx.files.internal(name);
        if (direct.exists()) {
            return direct;
        }
        return Gdx.files.internal("assets/" + name);
    }

    private Model loadModel(String internalPath) {
        FileHandle file = resolveAsset(internalPath);
        if (!file.exists()) {
            Gdx.app.error("ModelFactory", "Model file not found: " + internalPath);
            throw new RuntimeException("Model file not found: " + internalPath);
        }
        return new G3dModelLoader(new UBJsonReader()).loadModel(file);
    }

    @Override
    public void dispose() {
    }
}