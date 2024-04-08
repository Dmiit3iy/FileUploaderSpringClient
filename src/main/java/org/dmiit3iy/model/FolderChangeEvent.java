package org.dmiit3iy.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FolderChangeEvent {
    private  String action;
    private  String path;
}
