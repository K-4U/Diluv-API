package com.diluv.api.database;

import java.util.List;
import java.util.stream.Collectors;

import com.diluv.api.database.dao.ProjectDAO;
import com.diluv.api.database.record.ProjectFileRecord;
import com.diluv.api.database.record.ProjectRecord;
import com.diluv.api.database.record.ProjectTypeRecord;
import com.diluv.api.utils.FileReader;

public class ProjectTestDatabase implements ProjectDAO {

    private final List<ProjectRecord> projectRecords;
    private final List<ProjectTypeRecord> projectTypeRecords;
    private final List<ProjectFileRecord> projectFileRecords;

    public ProjectTestDatabase () {

        this.projectRecords = FileReader.readJsonFolder("records/projects", ProjectRecord.class);
        this.projectTypeRecords = FileReader.readJsonFolder("records/project_types", ProjectTypeRecord.class);
        this.projectFileRecords = FileReader.readJsonFolder("records/project_files", ProjectFileRecord.class);
    }

    @Override
    public List<ProjectRecord> findAllByUserId (long userId) {

        return this.projectRecords.stream().filter(projectRecord -> projectRecord.getUserId() == userId).collect(Collectors.toList());
    }

    @Override
    public List<ProjectTypeRecord> findAllProjectTypesByGameSlug (String gameSlug) {

        return this.projectTypeRecords.stream().filter(projectRecord -> projectRecord.getGameSlug().equals(gameSlug)).collect(Collectors.toList());
    }

    @Override
    public List<ProjectRecord> findAllProjectsByGameSlugAndProjectType (String gameSlug, String projectTypeSlug) {

        return this.projectRecords.stream().filter(projectRecord -> projectRecord.getGameSlug().equals(gameSlug) && projectRecord.getProjectTypeSlug().equals(projectTypeSlug)).collect(Collectors.toList());
    }

    @Override
    public ProjectTypeRecord findOneProjectTypeByGameSlugAndProjectTypeSlug (String gameSlug, String projectTypeSlug) {

        return this.projectTypeRecords.stream()
            .filter(projectRecord -> projectRecord.getGameSlug().equals(gameSlug) && projectRecord.getSlug().equals(projectTypeSlug))
            .findAny()
            .orElse(null);
    }

    @Override
    public ProjectRecord findOneProjectByGameSlugAndProjectTypeSlugAndProjectSlug (String gameSlug, String projectTypeSlug, String projectSlug) {

        return this.projectRecords.stream()
            .filter(projectRecord -> projectRecord.getGameSlug().equals(gameSlug) && projectRecord.getProjectTypeSlug().equals(projectTypeSlug) && projectRecord.getSlug().equals(projectSlug))
            .findAny()
            .orElse(null);
    }

    @Override
    public List<ProjectFileRecord> findAllProjectFilesByGameSlugAndProjectType (String gameSlug, String projectTypeSlug, String projectSlug) {

        ProjectRecord project = this.findOneProjectByGameSlugAndProjectTypeSlugAndProjectSlug(gameSlug, projectTypeSlug, projectSlug);
        if (project == null) {
            return null;
        }
        return this.projectFileRecords.stream().filter(projectRecord -> projectRecord.getProjectId() == project.getId()).collect(Collectors.toList());
    }

    @Override
    public boolean insertProject (String slug, String name, String summary, String description, String logo, long userId, String gameSlug, String projectTypeSlug) {

        this.projectRecords.add(new ProjectRecord(this.projectRecords.size(), name, slug, summary, description, logo, 0L, System.currentTimeMillis(), System.currentTimeMillis(), userId, gameSlug, projectTypeSlug));
        return true;
    }
}
