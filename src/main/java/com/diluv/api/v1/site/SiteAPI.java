package com.diluv.api.v1.site;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.Cache;

import com.diluv.api.data.DataBaseProject;
import com.diluv.api.data.DataBaseProjectType;
import com.diluv.api.data.DataGame;
import com.diluv.api.data.DataGameList;
import com.diluv.api.data.DataProjectType;
import com.diluv.api.data.DataTag;
import com.diluv.api.data.site.DataSiteGameProjects;
import com.diluv.api.data.site.DataSiteIndex;
import com.diluv.api.utils.Pagination;
import com.diluv.api.utils.error.ErrorMessage;
import com.diluv.api.utils.response.ResponseUtil;
import com.diluv.api.v1.games.GamesAPI;
import com.diluv.confluencia.database.record.GameRecord;
import com.diluv.confluencia.database.record.ProjectRecord;
import com.diluv.confluencia.database.record.ProjectTypeRecord;
import com.diluv.confluencia.database.record.TagRecord;
import com.diluv.confluencia.database.sort.GameSort;
import com.diluv.confluencia.database.sort.ProjectSort;

import static com.diluv.api.Main.DATABASE;

@GZIP
@Path("/site")
@Produces(MediaType.APPLICATION_JSON)
public class SiteAPI {

    @Cache(maxAge = 300, mustRevalidate = true)
    @GET
    @Path("/")
    public Response getIndex () {

        final List<GameRecord> gameRecords = DATABASE.gameDAO.findFeaturedGames();
        final List<DataGame> games = gameRecords.stream().map(DataGame::new).collect(Collectors.toList());

        final List<ProjectRecord> projectRecords = DATABASE.projectDAO.findFeaturedProjects();

        final List<DataBaseProject> projects = projectRecords.stream().map(projectRecord -> {
            final List<TagRecord> tagRecords = DATABASE.projectDAO.findAllTagsByProjectId(projectRecord.getId());
            List<DataTag> tags = tagRecords.stream().map(DataTag::new).collect(Collectors.toList());
            return new DataBaseProject(projectRecord, tags);
        }).collect(Collectors.toList());

        final long projectCount = DATABASE.projectDAO.countAll();
        final long userCount = DATABASE.userDAO.countAll();

        return ResponseUtil.successResponse(new DataSiteIndex(games, projects, projectCount, userCount));
    }

    @Cache(maxAge = 300, mustRevalidate = true)
    @GET
    @Path("/games")
    public Response getGames (@QueryParam("sort") String sort) {

        final List<GameRecord> gameRecords = DATABASE.gameDAO.findAll(GameSort.fromString(sort, GameSort.NAME));

        final List<DataGame> games = gameRecords.stream().map(DataGame::new).collect(Collectors.toList());
        return ResponseUtil.successResponse(new DataGameList(games, GamesAPI.GAME_SORTS));
    }

    @Cache(maxAge = 300, mustRevalidate = true)
    @GET
    @Path("/games/{gameSlug}")
    public Response getGameDefaultType (@PathParam("gameSlug") String gameSlug) {

        final GameRecord gameRecord = DATABASE.gameDAO.findOneBySlug(gameSlug);
        if (gameRecord == null) {

            return ErrorMessage.NOT_FOUND_GAME.respond();
        }

        // TODO GameRecord should store it's "default" project type and return it here instead of doing this.
        final List<ProjectTypeRecord> projectTypeRecords = DATABASE.projectDAO.findAllProjectTypesByGameSlug(gameSlug);
        return ResponseUtil.successResponse(projectTypeRecords.get(0).getSlug());
    }


    @GET
    @Path("/games/{gameSlug}/{projectTypeSlug}/projects")
    public Response getProjects (@PathParam("gameSlug") String gameSlug, @PathParam("projectTypeSlug") String projectTypeSlug, @QueryParam("page") Long queryPage, @QueryParam("limit") Integer queryLimit, @QueryParam("sort") String sort, @QueryParam("version") String version) {

        long page = Pagination.getPage(queryPage);
        int limit = Pagination.getLimit(queryLimit);
        final List<ProjectRecord> projectRecords;

        if (version == null) {
            projectRecords = DATABASE.projectDAO.findAllProjectsByGameSlugAndProjectType(gameSlug, projectTypeSlug, page, limit, ProjectSort.fromString(sort, ProjectSort.POPULARITY));
        }
        else {
            projectRecords = DATABASE.projectDAO.findAllProjectsByGameSlugAndProjectTypeAndVersion(gameSlug, projectTypeSlug, page, limit, ProjectSort.fromString(sort, ProjectSort.POPULARITY), version);
        }

        if (projectRecords.isEmpty()) {

            if (DATABASE.gameDAO.findOneBySlug(gameSlug) == null) {

                return ErrorMessage.NOT_FOUND_GAME.respond();
            }

            if (DATABASE.projectDAO.findOneProjectTypeByGameSlugAndProjectTypeSlug(gameSlug, projectTypeSlug) == null) {

                return ErrorMessage.NOT_FOUND_PROJECT_TYPE.respond();
            }
        }
        final List<DataBaseProject> projects = projectRecords.stream().map(projectRecord -> {
            final List<TagRecord> tagRecords = DATABASE.projectDAO.findAllTagsByProjectId(projectRecord.getId());
            List<DataTag> tags = tagRecords.stream().map(DataTag::new).collect(Collectors.toList());
            return new DataBaseProject(projectRecord, tags);
        }).collect(Collectors.toList());

        List<DataBaseProjectType> types = DATABASE.projectDAO.findAllProjectTypesByGameSlug(gameSlug).stream().map(DataBaseProjectType::new).collect(Collectors.toList());
        ProjectTypeRecord currentType = DATABASE.projectDAO.findOneProjectTypeByGameSlugAndProjectTypeSlug(gameSlug, projectTypeSlug);
        List<DataTag> tags = DATABASE.projectDAO.findAllTagsByGameSlugAndProjectTypeSlug(gameSlug, projectTypeSlug).stream().map(DataTag::new).collect(Collectors.toList());
        return ResponseUtil.successResponse(new DataSiteGameProjects(projects, types, new DataProjectType(currentType, tags), GamesAPI.PROJECT_SORTS));
    }
}
