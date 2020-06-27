package com.wire.bots.recording.utils;


import com.wire.bots.sdk.exceptions.HttpException;
import com.wire.bots.sdk.models.MessageAssetBase;
import com.wire.bots.sdk.server.model.User;
import com.wire.bots.sdk.tools.Logger;
import com.wire.bots.sdk.tools.Util;
import com.wire.bots.sdk.user.API;
import com.wire.bots.sdk.user.LoginClient;
import com.wire.bots.sdk.user.model.Access;

import javax.ws.rs.client.Client;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.UUID;

public class InstantCache extends Cache {
    private final String email;
    private final String password;
    private final Client client;
    private API api;

    public InstantCache(String email, String password, Client client) throws HttpException {
        super(null);
        this.email = email;
        this.password = password;
        this.client = client;
        Access access = new LoginClient(client).login(email, password);
        this.api = new API(client, null, access.getToken());
    }

    @Override
    protected User getUserObject(UUID userId) {
        try {
            return api.getUser(userId);
        } catch (Exception e) {
            Logger.error("InstantCache.getUserObject: userId: %s, ex: %s", userId, e);
            User ret = new User();
            ret.id = userId;
            ret.name = userId.toString();
            return ret;
        }
    }

    @Override
    protected byte[] downloadAsset(MessageAssetBase message) throws Exception {
        byte[] cipher;
        try {
            cipher = api.downloadAsset(message.getAssetKey(), message.getAssetToken());
        } catch (HttpException e) {
            if (e.getCode() == 401) {
                Access access = new LoginClient(client).login(email, password);
                this.api = new API(client, null, access.getToken());
                cipher = api.downloadAsset(message.getAssetKey(), message.getAssetToken());
            } else {
                throw e;
            }
        }
        byte[] sha256 = MessageDigest.getInstance("SHA-256").digest(cipher);
        if (!Arrays.equals(sha256, message.getSha256()))
            throw new Exception("Failed sha256 check");

        return Util.decrypt(message.getOtrKey(), cipher);
    }

    @Override
    protected User getUserInternal(UUID userId) throws HttpException {
        return api.getUser(userId);
    }

    @Override
    protected byte[] downloadProfilePicture(String key) throws Exception {
        return api.downloadAsset(key, null);
    }
}
