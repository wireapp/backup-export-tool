//
// Wire
// Copyright (C) 2016 Wire Swiss GmbH
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see http://www.gnu.org/licenses/.
//

package com.wire.backups.exports.exporters;

import com.waz.model.Messages;
import com.wire.xenon.models.*;
import com.wire.xenon.tools.Logger;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class GenericMessageConverter {
    private final static ConcurrentHashMap<UUID, Messages.Asset.Original> originals = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<UUID, Messages.Asset.RemoteData> remotes = new ConcurrentHashMap<>();

    public static MessageBase convert(UUID from, String clientId, UUID convId, String time, Messages.GenericMessage generic) {
        UUID messageId = UUID.fromString(generic.getMessageId());

        Messages.Asset asset = null;

        Logger.debug("msgId: %s hasText: %s, hasAsset: %s",
                messageId,
                generic.hasText(),
                generic.hasAsset());

        // Ephemeral messages
        if (generic.hasEphemeral()) {
            Messages.Ephemeral ephemeral = generic.getEphemeral();

            if (ephemeral.hasText() && ephemeral.getText().hasContent()) {
                EphemeralTextMessage msg = new EphemeralTextMessage(messageId, convId, clientId, from);
                msg.setTime(time);
                msg.setExpireAfterMillis(ephemeral.getExpireAfterMillis());
                msg.setText(ephemeral.getText().getContent());
                if (ephemeral.getText().hasQuote()) {
                    final String quotedMessageId = ephemeral.getText().getQuote().getQuotedMessageId();
                    msg.setQuotedMessageId(UUID.fromString(quotedMessageId));
                }
                for (Messages.Mention mention : ephemeral.getText().getMentionsList())
                    msg.addMention(mention.getUserId(), mention.getStart(), mention.getLength());

                return msg;
            }

            if (ephemeral.hasAsset()) {
                asset = ephemeral.getAsset();
            }
        }

        // Edit message
        if (generic.hasEdited() && generic.getEdited().hasText()) {
            Messages.MessageEdit edited = generic.getEdited();
            EditedTextMessage msg = new EditedTextMessage(messageId, convId, clientId, from);
            msg.setReplacingMessageId(UUID.fromString(edited.getReplacingMessageId()));
            msg.setText(edited.getText().getContent());
            msg.setTime(time);
            for (Messages.Mention mention : edited.getText().getMentionsList())
                msg.addMention(mention.getUserId(), mention.getStart(), mention.getLength());

            return msg;
        }

        if (generic.hasConfirmation()) {
            Messages.Confirmation confirmation = generic.getConfirmation();
            ConfirmationMessage msg = new ConfirmationMessage(messageId, convId, clientId, from);

            return handleConfirmation(confirmation, msg, time);
        }

        // Text
        if (generic.hasText()) {
            Messages.Text text = generic.getText();
            List<Messages.LinkPreview> linkPreviewList = text.getLinkPreviewList();

            if (!linkPreviewList.isEmpty()) {
                LinkPreviewMessage msg = new LinkPreviewMessage(messageId, convId, clientId, from);
                String content = text.getContent();

                return handleLinkPreview(linkPreviewList, content, msg, time);
            }

            if (text.hasContent()) {
                TextMessage msg = new TextMessage(messageId, convId, clientId, from);
                msg.setText(text.getContent());
                msg.setTime(time);
                if (text.hasQuote()) {
                    final String quotedMessageId = text.getQuote().getQuotedMessageId();
                    msg.setQuotedMessageId(UUID.fromString(quotedMessageId));
                }
                for (Messages.Mention mention : text.getMentionsList())
                    msg.addMention(mention.getUserId(), mention.getStart(), mention.getLength());

                return msg;
            }
        }

        if (generic.hasCalling()) {
            Messages.Calling calling = generic.getCalling();
            if (calling.hasContent()) {
                CallingMessage message = new CallingMessage(messageId, convId, clientId, from);
                message.setContent(calling.getContent());
                message.setTime(time);
                return message;
            }
        }

        if (generic.hasDeleted()) {
            DeletedTextMessage msg = new DeletedTextMessage(messageId, convId, clientId, from);
            UUID delMsgId = UUID.fromString(generic.getDeleted().getMessageId());
            msg.setDeletedMessageId(delMsgId);
            msg.setTime(time);

            return msg;
        }

        if (generic.hasReaction()) {
            Messages.Reaction reaction = generic.getReaction();
            ReactionMessage msg = new ReactionMessage(messageId, convId, clientId, from);
            return handleReaction(reaction, msg, time);
        }

        if (generic.hasKnock()) {
            PingMessage msg = new PingMessage(messageId, convId, clientId, from);
            msg.setTime(time);

            return msg;
        }

        // Assets
        if (generic.hasAsset()) {
            asset = generic.getAsset();
        }

        if (asset != null) {
            Logger.debug("Asset: msgId: %s hasOriginal: %s, hasUploaded: %s, hasPreview: %s",
                    messageId,
                    asset.hasOriginal(),
                    asset.hasUploaded(),
                    asset.hasPreview());

            if (asset.hasPreview()) {
                ImageMessage msg = new ImageMessage(messageId, convId, clientId, from);
                return handleVideoPreview(asset.getPreview(), msg, time);
            }

            if (asset.hasUploaded()) {
                remotes.put(messageId, asset.getUploaded());
            }

            if (asset.hasOriginal()) {
                originals.put(messageId, asset.getOriginal());
            }

            Messages.Asset.Original original = originals.get(messageId);
            Messages.Asset.RemoteData remoteData = remotes.get(messageId);

            MessageAssetBase base = new MessageAssetBase(messageId, convId, clientId, from);
            base.setTime(time);
            base.fromOrigin(original);
            base.fromRemote(remoteData);

            if (base.getAssetKey() != null) {
                if (original != null) {
                    if (original.hasImage()) {
                        return new ImageMessage(base, original.getImage());
                    }
                    if (original.hasAudio()) {
                        return new AudioMessage(base, original.getAudio());
                    }
                    if (original.hasVideo()) {
                        return new VideoMessage(base, original.getVideo());
                    }
                }

                {
                    return new AttachmentMessage(base);
                }
            }
        }

        return null;
    }

    private static MessageBase handleConfirmation(Messages.Confirmation confirmation, ConfirmationMessage msg, String time) {
        String firstMessageId = confirmation.getFirstMessageId();
        Messages.Confirmation.Type type = confirmation.getType();

        msg.setConfirmationMessageId(UUID.fromString(firstMessageId));
        msg.setType(type.getNumber() == Messages.Confirmation.Type.DELIVERED_VALUE
                ? ConfirmationMessage.Type.DELIVERED
                : ConfirmationMessage.Type.READ);
        msg.setTime(time);

        return msg;
    }

    private static MessageBase handleLinkPreview(List<Messages.LinkPreview> linkPreviewList, String content, LinkPreviewMessage msg, String time) {
        for (Messages.LinkPreview link : linkPreviewList) {
            Messages.Asset image = link.getImage();

            msg.fromOrigin(image.getOriginal());
            msg.fromRemote(image.getUploaded());

            msg.setTime(time);
            msg.setHeight(image.getOriginal().getImage().getHeight());
            msg.setWidth(image.getOriginal().getImage().getWidth());

            msg.setSummary(link.getSummary());
            msg.setTitle(link.getTitle());
            msg.setUrl(link.getUrl());
            msg.setUrlOffset(link.getUrlOffset());

            msg.setText(content);
            return msg;
        }
        return null;
    }

    private static MessageBase handleReaction(Messages.Reaction reaction, ReactionMessage msg, String time) {
        if (reaction.hasEmoji()) {
            msg.setEmoji(reaction.getEmoji());
            msg.setReactionMessageId(UUID.fromString(reaction.getMessageId()));
            msg.setTime(time);

            return msg;
        }
        return null;
    }

    private static MessageBase handleVideoPreview(Messages.Asset.Preview preview, ImageMessage msg, String time) {
        if (preview.hasRemote()) {
            msg.setTime(time);
            msg.fromRemote(preview.getRemote());

            msg.setHeight(preview.getImage().getHeight());
            msg.setWidth(preview.getImage().getWidth());

            msg.setSize(preview.getSize());
            msg.setMimeType(preview.getMimeType());

            return msg;
        }
        return null;
    }
}
