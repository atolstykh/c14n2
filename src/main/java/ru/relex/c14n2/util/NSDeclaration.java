package ru.relex.c14n2.util;

/**
 *
 */
public class NSDeclaration {
    private String uri;
    private String prefix;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NSDeclaration that = (NSDeclaration) o;

        if (uri != null ? !uri.equals(that.uri) : that.uri != null) return false;
        return prefix != null ? prefix.equals(that.prefix) : that.prefix == null;

    }

    @Override
    public int hashCode() {
        int result = uri != null ? uri.hashCode() : 0;
        result = 31 * result + (prefix != null ? prefix.hashCode() : 0);
        return result;
    }

}
