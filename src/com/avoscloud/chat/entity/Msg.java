package com.avoscloud.chat.entity;

import com.alibaba.fastjson.JSON;
import com.avos.avoscloud.AVMessage;
import com.avos.avoscloud.AVUtils;
import com.avoscloud.chat.R;
import com.avoscloud.chat.avobject.User;
import com.avoscloud.chat.base.App;
import com.avoscloud.chat.service.ChatService;
import com.avoscloud.chat.service.EmotionService;
import com.avoscloud.chat.util.PathUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by lzw on 14-8-7.
 */
public class Msg {
  public static final int STATUS_SEND_START = 0;
  public static final int STATUS_SEND_SUCCEED = 1;
  public static final int STATUS_SEND_RECEIVED = 2;
  public static final int STATUS_SEND_FAILED = 3;
  public static final int TYPE_TEXT = 0;
  public static final int TYPE_RESPONSE = 1;
  public static final int TYPE_IMAGE = 2;
  public static final int TYPE_AUDIO = 3;
  public static final int TYPE_LOCATION = 4;
  //long timestamp;
  //String fromPeerId;
  //List<String> toPeerIds;
  String content;
  String objectId;
  String convid;
  boolean singleChat = true;

  AVMessage internalMessage;
  int status = STATUS_SEND_START;
  int type = TYPE_TEXT;


  public Msg() {
    internalMessage = new AVMessage();
  }

  public AVMessage getInternalMessage() {
    return internalMessage;
  }

  public void setInternalMessage(AVMessage internalMessage) {
    this.internalMessage = internalMessage;
  }

  public String getToPeerId() {
    List<String> toPeerIds = internalMessage.getToPeerIds();
    if (toPeerIds != null && toPeerIds.size() > 0) {
      return toPeerIds.get(0);
    } else {
      return null;
    }
  }

  private List<String> getToPeerIds() {
    return internalMessage.getToPeerIds();
  }

  public void setToPeerId(String toPeerId) {
    setToPeerIds(Arrays.asList(toPeerId));
  }

  private void setToPeerIds(List<String> toPeerIds) {
    internalMessage.setToPeerIds(toPeerIds);
  }

  public String getFromPeerId() {
    return internalMessage.getFromPeerId();
  }

  public void setFromPeerId(String fromPeerId) {
    internalMessage.setFromPeerId(fromPeerId);
  }

  public long getTimestamp() {
    return internalMessage.getTimestamp();
  }

  public String getConvid() {
    return convid;
  }

  public void setConvid(String convid) {
    this.convid = convid;
  }

  public boolean isSingleChat() {
    return singleChat;
  }

  public void setSingleChat(boolean singleChat) {
    this.singleChat = singleChat;
  }

  public void setTimestamp(long timestamp) {
    internalMessage.setTimestamp(timestamp);
  }

  public String getContent() {
    return content;
  }

  public int getStatus() {
    return status;
  }

  public String getStatusDesc() {
    if (status == STATUS_SEND_START) {
      return App.ctx.getString(R.string.sending);
    } else if (status == STATUS_SEND_RECEIVED) {
      return App.ctx.getString(R.string.received);
    } else if (status == STATUS_SEND_SUCCEED) {
      return App.ctx.getString(R.string.sent);
    } else if (status == STATUS_SEND_FAILED) {
      return App.ctx.getString(R.string.failed);
    } else {
      throw new IllegalArgumentException("unknown status");
    }
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public boolean isComeMessage() {
    String fromPeerId = getFromPeerId();
    return !fromPeerId.equals(ChatService.getSelfId());
  }

  public String getChatUserId() {
    if (isSingleChat() == false) {
      throw new UnsupportedOperationException("unsupport for not singleChat");
    } else {
      String fromPeerId = getFromPeerId();
      String selfId = ChatService.getSelfId();
      if (fromPeerId == null || selfId == null) {
        throw new NullPointerException("fromPeerId is null or selfId is null");
      }
      if (fromPeerId.equals(selfId)) {
        return getToPeerId();
      } else {
        return fromPeerId;
      }
    }
  }

  public String getFromName() {
    String peerId = getFromPeerId();
    User user = App.lookupUser(peerId);
    return user.getUsername();
  }

  public CharSequence getNotifyContent() {
    switch (type) {
      case TYPE_AUDIO:
        return App.ctx.getString(R.string.audio);
      case TYPE_TEXT:
        if (EmotionService.haveEmotion(getContent())) {
          return App.ctx.getString(R.string.emotion);
        } else {
          return getContent();
        }
      case TYPE_IMAGE:
        return App.ctx.getString(R.string.image);
      case TYPE_LOCATION:
        return App.ctx.getString(R.string.position);
      default:
        return App.ctx.getString(R.string.newMessage);
    }
  }

  public static Msg fromAVMessage(AVMessage avMsg) {
    Msg msg = new Msg();
    msg.setInternalMessage(avMsg);
    if (!AVUtils.isBlankString(avMsg.getMessage())) {
      HashMap<String, Object> params = JSON.parseObject(avMsg.getMessage(), HashMap.class);
      msg.setObjectId((String) params.get("objectId"));
      msg.setContent((String) params.get("content"));
      msg.setStatus((Integer) params.get("status"));
      msg.setType((Integer) params.get("type"));
      msg.setSingleChat((Boolean) params.get("singleChat"));
      msg.setConvid((String) params.get("convid"));
    }
    return msg;
  }

  public AVMessage toAVMessage() {
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put("objectId", objectId);
    params.put("content", content);
    params.put("status", status);
    params.put("type", type);
    params.put("singleChat", singleChat);
    params.put("convid", convid);
    internalMessage.setMessage(JSON.toJSONString(params));
    return internalMessage;
  }

  @Override
  public String toString() {
    return "{content:" + getContent() + " objectId:" + getObjectId() + " status:" + getStatus() + " fromPeerId:" +
        getFromPeerId() + " toPeerIds:" + getToPeerIds()
        + " timestamp:" + getTimestamp() + " type=" + getType() + "}";
  }

  public String getAudioPath() {
    return PathUtils.getChatFileDir() + getObjectId();
  }
}