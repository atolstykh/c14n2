package ru.relex.c14n2.util;

/**
 *
 */
public class NSDeclaration implements Comparable<NSDeclaration>{
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


    private int compare(String o1, String o2) {
        if( o1 == o2 )
            return 0;
        if( o1 == null )
            return 1;
        if( o2 == null )
            return -1;
        return o1.compareTo( o2 );
    }

    @Override
    public int compareTo(NSDeclaration nsDeclaration) {
        int compareUriResult = compare(uri, nsDeclaration.uri);
        if (compareUriResult==0) {
            return compare(prefix,nsDeclaration.prefix);
        } else {
            return compareUriResult;
        }
    }
}
