/*
    Mango - Open Source M2M - http://mango.serotoninsoftware.com
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc.
    @author Matthew Lohbihler
    
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.serotonin.mango.view;

import java.util.ArrayList;
import java.util.List;

public class ImageSet extends ViewGraphic {
    private final List<String> imageFilenames = new ArrayList<String>();

    ImageSet(String id, String name, String[] imageFilenames, int width, int height, int textX, int textY) {
        super(id, name, width, height, textX, textY);
        for (String filename : imageFilenames)
            this.imageFilenames.add(filename);
    }

    @Override
    public boolean isImageSet() {
        return true;
    }

    public int getImageCount() {
        return imageFilenames.size();
    }

    public String getImageFilename(int index) {
        return imageFilenames.get(index);
    }

    public List<String> getImageFilenames() {
        return imageFilenames;
    }
}
