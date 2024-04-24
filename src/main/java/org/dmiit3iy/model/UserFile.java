package org.dmiit3iy.model;

import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class UserFile {

    private long id;
    @NonNull
    private String name;
    @NonNull
    private String hash;
    @NonNull
    private int version;
    private String path;
    @Override
    public String toString() {
        return name;
    }

}
