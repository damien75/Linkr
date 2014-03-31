package sara.damien.app.model;

/**
 * Created by Damien on 14/02/2014.
 */
public class NavDrawerItem {
    private String title;
    private int icon;
    private String subtitle = "0";
    private boolean subtitleVisible = false;

    public NavDrawerItem(String title, int icon) {
        this.title = title;
        this.subtitleVisible = false;
        this.icon = icon;
    }

    public NavDrawerItem(String title, String subtitle, int icon) {
        this.title = title;
        this.subtitle = subtitle;
        this.subtitleVisible = true;
        this.icon = icon;
    }

    public String getTitle() {
        return this.title;
    }

    public int getIcon() {
        return this.icon;
    }

    public String getSubtitle() {
        return subtitleVisible ? this.subtitle : null;
    }

    public boolean isSubtitleVisible() {
        return this.subtitleVisible;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public void setSubtitleVisible(boolean subtitleVisible) {
        this.subtitleVisible = subtitleVisible;
    }
}
