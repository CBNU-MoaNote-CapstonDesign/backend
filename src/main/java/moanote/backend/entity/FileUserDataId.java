package moanote.backend.entity;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class FileUserDataId implements Serializable {

  @EqualsAndHashCode.Include
  private File file;

  @EqualsAndHashCode.Include
  private UserData user;
}
