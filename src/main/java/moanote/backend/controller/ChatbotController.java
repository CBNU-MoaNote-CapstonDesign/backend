package moanote.backend.controller;


import moanote.backend.dto.UserChatMessageBroadcastDTO;
import moanote.backend.service.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/agent")
public class ChatbotController {

  @Autowired
  private ChatbotService chatbotService;

  @PostMapping("/chat/{channelId}")
  public ResponseEntity<?> chat(String body, @PathVariable String channelId) {
    UserChatMessageBroadcastDTO message = chatbotService.handleBuildChat(channelId);

    System.out.println("Build AI Message in channel: " + channelId);

    return ResponseEntity.status(200).body(message);
  }

  @PostMapping("/chat/{channelId}/{chatId}")
  public ResponseEntity<?> edit(
      @RequestBody UserChatMessageBroadcastDTO message,
      @PathVariable String channelId) {

    System.out.println("Edit AI Message in channel: " + channelId);
    System.out.println(message.toString());

    chatbotService.handleEditChat(channelId, message);

    return ResponseEntity.status(200).body(message);
  }
}