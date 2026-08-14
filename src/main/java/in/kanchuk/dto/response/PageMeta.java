package in.kanchuk.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PageMeta {
    private long total;
    private int page;
    private int limit;
    private int totalPages;
}
