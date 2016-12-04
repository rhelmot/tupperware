import java.util.*;

public class SearchPostsWindow extends PostsWindow {
    private String[] tagsAnd;
    private String[] tagsOr;

    public SearchPostsWindow(Tupperware root, String[] tagsAnd, String tagsOr[]) {
        super(root, "Tag Search");

        int tagCount = 0;
        if (tagsAnd != null) tagCount += tagsAnd.length;
        if (tagsOr != null) tagCount += tagsOr.length;

        if (tagCount == 1) {
            setTitle("Searching #" + (tagsAnd == null ? tagsOr[0] : tagsAnd[0]));
        } else {
            setTitle("Searching " + tagCount + " tags");
        }

        this.tagsAnd = tagsAnd;
        this.tagsOr = tagsOr;
    }

    @Override
    protected ArrayList<PostsEntity> loadPosts(PostsEntity after, int count) {
        return PostsEntity.tagSearch(tagsAnd, tagsOr, after, count);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SearchPostsWindow) {
            SearchPostsWindow x = (SearchPostsWindow) other;
            return Arrays.equals(tagsAnd, x.tagsAnd) && Arrays.equals(tagsOr, x.tagsOr);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return 9192 + Arrays.deepHashCode(tagsAnd) * 33411 + Arrays.deepHashCode(tagsOr) * 52209;
    }
}
