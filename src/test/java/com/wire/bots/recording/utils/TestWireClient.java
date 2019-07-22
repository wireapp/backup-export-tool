package com.wire.bots.recording.utils;

import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.assets.IAsset;
import com.wire.bots.sdk.assets.IGeneric;
import com.wire.bots.sdk.models.AssetKey;
import com.wire.bots.sdk.models.otr.PreKey;
import com.wire.bots.sdk.server.model.Asset;
import com.wire.bots.sdk.server.model.Conversation;
import com.wire.bots.sdk.server.model.User;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class TestWireClient implements WireClient {
    @Override
    public UUID sendText(String txt) {
        return null;
    }

    @Override
    public UUID sendDirectText(String txt, String userId) {
        return null;
    }

    @Override
    public UUID sendText(String txt, long expires) {
        return null;
    }

    @Override
    public UUID sendLinkPreview(String url, String title, IGeneric image) {
        return null;
    }

    @Override
    public UUID sendDirectLinkPreview(String url, String title, IGeneric image, String userId) {
        return null;
    }

    @Override
    public UUID sendPicture(byte[] bytes, String mimeType) {
        return null;
    }

    @Override
    public UUID sendDirectPicture(byte[] bytes, String mimeType, String userId) {
        return null;
    }

    @Override
    public UUID sendPicture(IGeneric image) {
        return null;
    }

    @Override
    public UUID sendDirectPicture(IGeneric image, String userId) {
        return null;
    }

    @Override
    public UUID sendAudio(byte[] bytes, String name, String mimeType, long duration) {
        return null;
    }

    @Override
    public UUID sendVideo(byte[] bytes, String name, String mimeType, long duration, int h, int w) {
        return null;
    }

    @Override
    public UUID sendFile(File file, String mime) {
        return null;
    }

    @Override
    public UUID sendDirectFile(File file, String mime, String userId) {
        return null;
    }

    @Override
    public UUID sendDirectFile(IGeneric preview, IGeneric asset, String userId) {
        return null;
    }

    @Override
    public UUID ping() {
        return null;
    }

    @Override
    public UUID sendReaction(UUID msgId, String emoji) {
        return null;
    }

    @Override
    public UUID deleteMessage(UUID msgId) {
        return null;
    }

    @Override
    public UUID editMessage(UUID replacingMessageId, String text) {
        return null;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public User getSelf() {
        return null;
    }

    @Override
    public UUID getConversationId() {
        return null;
    }

    @Override
    public String getDeviceId() {
        return null;
    }

    @Override
    public Collection<User> getUsers(Collection<String> userIds) {
        return null;
    }

    @Override
    public User getUser(String userId) {
        User user = new User();
        user.id = UUID.fromString(userId);
        user.assets = new ArrayList<>();
        Asset asset = new Asset();
        asset.key = userId;
        asset.size = "preview";
        asset.type = "image/png";
        user.assets.add(asset);
        return user;
    }

    @Override
    public Conversation getConversation() {
        return null;
    }

    @Override
    public void acceptConnection(UUID user) {

    }

    @Override
    public String decrypt(String userId, String clientId, String cypher) {
        return null;
    }

    @Override
    public PreKey newLastPreKey() {
        return null;
    }

    @Override
    public ArrayList<PreKey> newPreKeys(int from, int count) {
        return null;
    }

    @Override
    public void uploadPreKeys(ArrayList<PreKey> preKeys) {

    }

    @Override
    public ArrayList<Integer> getAvailablePrekeys() {
        return null;
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public byte[] downloadAsset(String assetKey, String assetToken, byte[] sha256Challenge, byte[] otrKey) throws Exception {
        String filename = String.format("src/test/resources/recording/images/%s.png", assetKey);
        return Files.readAllBytes(Paths.get(filename));
    }

    @Override
    public byte[] downloadProfilePicture(String assetKey) throws Exception {
        String filename = String.format("src/test/resources/recording/avatars/%s.png", assetKey);
        return Files.readAllBytes(Paths.get(filename));
    }

    @Override
    public AssetKey uploadAsset(IAsset asset) {
        return null;
    }

    @Override
    public void call(String content) {

    }

    @Override
    public void close() {

    }
}
