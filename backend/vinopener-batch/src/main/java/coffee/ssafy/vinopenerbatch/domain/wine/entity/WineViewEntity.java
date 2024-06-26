package coffee.ssafy.vinopenerbatch.domain.wine.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "wineView")
@AllArgsConstructor
@Getter
@Builder
public class WineViewEntity {

    @Id
    private Long wineId;
    private Long viewCount;

    public WineViewEntity increaseViewCount() {
        this.viewCount++;
        return this;
    }

    public void setView(Long view) {
        this.viewCount = view;
    }


}
