package moanote.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Set;
import java.util.HashSet;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "text_note_segment")
public class TextNoteSegment extends BaseNoteSegment {

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "segment", orphanRemoval = true)
  private Set<FugueNode> nodes = new HashSet<>();

  public void addNode(FugueNode node) {
    node.setSegment(this);
    nodes.add(node);
  }

  @OneToOne(cascade = CascadeType.ALL, optional = false)
  @JoinColumns(value = {
      @JoinColumn(name = "root_node_id", referencedColumnName = "id"),
      @JoinColumn(name = "root_segment_id", referencedColumnName = "segment_id")
  })
  private FugueNode rootNode;
}