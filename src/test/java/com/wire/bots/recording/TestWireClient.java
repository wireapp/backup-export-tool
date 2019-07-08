package com.wire.bots.recording;

import com.wire.bots.cryptobox.CryptoException;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.assets.IAsset;
import com.wire.bots.sdk.assets.IGeneric;
import com.wire.bots.sdk.models.AssetKey;
import com.wire.bots.sdk.models.otr.PreKey;
import com.wire.bots.sdk.server.model.Asset;
import com.wire.bots.sdk.server.model.Conversation;
import com.wire.bots.sdk.server.model.User;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class TestWireClient implements WireClient {
    @Override
    public UUID sendText(String txt) throws Exception {
        return null;
    }

    @Override
    public UUID sendDirectText(String txt, String userId) throws Exception {
        return null;
    }

    @Override
    public UUID sendText(String txt, long expires) throws Exception {
        return null;
    }

    @Override
    public UUID sendLinkPreview(String url, String title, IGeneric image) throws Exception {
        return null;
    }

    @Override
    public UUID sendDirectLinkPreview(String url, String title, IGeneric image, String userId) throws Exception {
        return null;
    }

    @Override
    public UUID sendPicture(byte[] bytes, String mimeType) throws Exception {
        return null;
    }

    @Override
    public UUID sendDirectPicture(byte[] bytes, String mimeType, String userId) throws Exception {
        return null;
    }

    @Override
    public UUID sendPicture(IGeneric image) throws Exception {
        return null;
    }

    @Override
    public UUID sendDirectPicture(IGeneric image, String userId) throws Exception {
        return null;
    }

    @Override
    public UUID sendAudio(byte[] bytes, String name, String mimeType, long duration) throws Exception {
        return null;
    }

    @Override
    public UUID sendVideo(byte[] bytes, String name, String mimeType, long duration, int h, int w) throws Exception {
        return null;
    }

    @Override
    public UUID sendFile(File file, String mime) throws Exception {
        return null;
    }

    @Override
    public UUID sendDirectFile(File file, String mime, String userId) throws Exception {
        return null;
    }

    @Override
    public UUID sendDirectFile(IGeneric preview, IGeneric asset, String userId) throws Exception {
        return null;
    }

    @Override
    public UUID ping() throws Exception {
        return null;
    }

    @Override
    public UUID sendReaction(UUID msgId, String emoji) throws Exception {
        return null;
    }

    @Override
    public UUID deleteMessage(UUID msgId) throws Exception {
        return null;
    }

    @Override
    public UUID editMessage(UUID replacingMessageId, String text) throws Exception {
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
    public Collection<User> getUsers(Collection<String> userIds) throws IOException {
        return null;
    }

    @Override
    public User getUser(String userId) {
        User user = new User();
        user.id = userId;
        user.assets = new ArrayList<>();
        Asset asset = new Asset();
        asset.key = userId;
        asset.size = "preview";
        asset.type = "image/png";
        user.assets.add(asset);
        return user;
    }

    @Override
    public Conversation getConversation() throws IOException {
        return null;
    }

    @Override
    public void acceptConnection(UUID user) throws Exception {

    }

    @Override
    public String decrypt(String userId, String clientId, String cypher) throws CryptoException {
        return null;
    }

    @Override
    public PreKey newLastPreKey() throws CryptoException {
        return null;
    }

    @Override
    public ArrayList<PreKey> newPreKeys(int from, int count) throws CryptoException {
        return null;
    }

    @Override
    public void uploadPreKeys(ArrayList<PreKey> preKeys) throws IOException {

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
        String filename = String.format("images/%s.png", assetKey);
        return Files.readAllBytes(Paths.get(filename));
    }

    @Override
    public byte[] downloadProfilePicture(String assetKey) throws Exception {
        String filename = String.format("avatars/%s.png", assetKey);
        return Files.readAllBytes(Paths.get(filename));
    }

    @Override
    public AssetKey uploadAsset(IAsset asset) throws Exception {
        return null;
    }

    @Override
    public void call(String content) throws Exception {

    }

    @Override
    public void close() throws IOException {

    }
}
