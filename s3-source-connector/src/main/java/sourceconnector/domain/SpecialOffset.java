package sourceconnector.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SpecialOffset {
  INITIAL_OFFSET(0L),
  END_OFFSET(-1L);

  private final long value;
}
