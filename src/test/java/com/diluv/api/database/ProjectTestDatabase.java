package com.diluv.api.database;

import java.util.List;

import com.diluv.api.database.dao.ProjectDAO;
import com.diluv.api.database.record.ProjectRecord;

public class ProjectTestDatabase implements ProjectDAO {
    @Override
    public List<ProjectRecord> findAllByUserId (long userId) {

        return null;
    }

    @Override
    public List<ProjectRecord> findAllByGameSlug (String gameSlug) {

        return null;
    }

    @Override
    public ProjectRecord findAllByGameSlugAndProjectSlug (String gameSlug, String projectSlug) {

        return null;
    }
}
