package moanote.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "text_chat_message")
public class TextChatMessage {

  /**
   * UUIDv7
   */
  @Id
  @Column(name = "id")
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "sender_id", nullable = false)
  private UserData sender;

  @Column(name = "content", nullable = false)
  private String content;

  /**
   * UTC+0
   */
  @Column(name = "sent_at", nullable = false)
  private OffsetDateTime sentAt;

  @Column(name = "message_type", nullable = false)
  private String messageType;
}
