/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.persistence.session.capability;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.cypher.query.Pagination;
import org.neo4j.ogm.cypher.query.SortOrder;
import org.neo4j.ogm.domain.music.Album;
import org.neo4j.ogm.domain.music.Artist;
import org.neo4j.ogm.domain.music.Recording;
import org.neo4j.ogm.domain.music.Studio;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @author Luanne Misquitta
 */
public class LoadCapabilityTest extends MultiDriverTestClass {

    private SessionFactory sessionFactory;
    private Session session;
    private Long pleaseId;
    private Long beatlesId;

    @Before
    public void init() throws IOException {
        sessionFactory = new SessionFactory("org.neo4j.ogm.domain.music");
        session = sessionFactory.openSession();
        session.purgeDatabase();
        //Create some data
        Artist theBeatles = new Artist("The Beatles");
        Album please = new Album("Please Please Me");
        theBeatles.getAlbums().add(please);
        please.setArtist(theBeatles);
        session.save(theBeatles);

        pleaseId = please.getId();
        beatlesId = theBeatles.getId();
    }

    @After
    public void clearDatabase() {
        session.purgeDatabase();
    }

    /**
     * @see DATAGRAPH-707
     */
    @Test
    public void loadAllShouldRespectEntityType() {
        Collection<Artist> artists = session.loadAll(Artist.class, Collections.singletonList(beatlesId));
        assertEquals(1, artists.size());
        assertEquals("The Beatles", artists.iterator().next().getName());

        Collection<Album> albums = session.loadAll(Album.class, Collections.singletonList(beatlesId));
        assertEquals(0, albums.size());

        artists = session.loadAll(Artist.class, Collections.singletonList(beatlesId), 0);
        assertEquals(1, artists.size());
        assertEquals("The Beatles", artists.iterator().next().getName());

        albums = session.loadAll(Album.class, Collections.singletonList(beatlesId), 0);
        assertEquals(0, albums.size());


        artists = session.loadAll(Artist.class, Collections.singletonList(beatlesId), new SortOrder().add("name"));
        assertEquals(1, artists.size());
        assertEquals("The Beatles", artists.iterator().next().getName());

        albums = session.loadAll(Album.class, Collections.singletonList(beatlesId), new SortOrder().add("name"));
        assertEquals(0, albums.size());


        artists = session.loadAll(Artist.class, Collections.singletonList(beatlesId), new SortOrder().add("name"), 0);
        assertEquals(1, artists.size());
        assertEquals("The Beatles", artists.iterator().next().getName());

        albums = session.loadAll(Album.class, Collections.singletonList(beatlesId), new SortOrder().add("name"), 0);
        assertEquals(0, albums.size());


        artists = session.loadAll(Artist.class, Collections.singletonList(beatlesId), new Pagination(0, 5));
        assertEquals(1, artists.size());
        assertEquals("The Beatles", artists.iterator().next().getName());

        albums = session.loadAll(Album.class, Collections.singletonList(beatlesId), new Pagination(0, 5));
        assertEquals(0, albums.size());


        artists = session.loadAll(Artist.class, Collections.singletonList(beatlesId), new Pagination(0, 5), 0);
        assertEquals(1, artists.size());
        assertEquals("The Beatles", artists.iterator().next().getName());

        albums = session.loadAll(Album.class, Collections.singletonList(beatlesId), new Pagination(0, 5), 0);
        assertEquals(0, albums.size());


        artists = session.loadAll(Artist.class, Collections.singletonList(beatlesId), new SortOrder().add("name"), new Pagination(0, 5));
        assertEquals(1, artists.size());
        assertEquals("The Beatles", artists.iterator().next().getName());

        albums = session.loadAll(Album.class, Collections.singletonList(beatlesId), new SortOrder().add("name"), new Pagination(0, 5));
        assertEquals(0, albums.size());


        artists = session.loadAll(Artist.class, Collections.singletonList(beatlesId), new SortOrder().add("name"), new Pagination(0, 5), 0);
        assertEquals(1, artists.size());
        assertEquals("The Beatles", artists.iterator().next().getName());

        Artist bonJovi = new Artist("Bon Jovi");
        session.save(bonJovi);

        artists = session.loadAll(Artist.class, Arrays.asList(beatlesId, pleaseId, bonJovi.getId()), new SortOrder().add("name"), new Pagination(0, 5), 0);
        assertEquals(2, artists.size());

        artists = session.loadAll(Artist.class, Collections.singletonList(beatlesId), new SortOrder().add("name"), new Pagination(0, 5), 0);
        assertEquals(1, artists.size());
        assertEquals("The Beatles", artists.iterator().next().getName()); //make sure Bon Jovi isn't returned as well

        albums = session.loadAll(Album.class, Collections.singletonList(beatlesId), new SortOrder().add("name"), new Pagination(0, 5), 0);
        assertEquals(0, albums.size());
    }


    /**
     * @see DATAGRAPH-707
     */
    @Test
    public void loadOneShouldRespectEntityType() {
        Artist artist = session.load(Artist.class, beatlesId);
        assertEquals("The Beatles", artist.getName());

        Album album = session.load(Album.class, beatlesId);
        assertNull(album);

        artist = session.load(Artist.class, beatlesId, 0);
        assertEquals("The Beatles", artist.getName());

        album = session.load(Album.class, beatlesId, 0);
        assertNull(album);

        artist = session.load(Artist.class, 10l); //ID does not exist
        assertNull(artist);
    }

	/**
     * @see Issue 170
     */
    @Test
    public void shouldBeAbleToLoadEntitiesToDifferentDepthsInDifferentSessions() {
        Artist pinkFloyd = new Artist("Pink Floyd");
        Album divisionBell = new Album("The Division Bell");
        divisionBell.setArtist(pinkFloyd);
        Studio studio = new Studio("Britannia Row Studios");
        Recording recording = new Recording(divisionBell, studio, 1994);
        divisionBell.setRecording(recording);
        pinkFloyd.addAlbum(divisionBell);
        session.save(pinkFloyd);
        session.clear();

        //Load Pink Floyd to depth 1 in a new session
        Session session1 = sessionFactory.openSession();
        Artist pinkfloyd1 = session1.load(Artist.class, pinkFloyd.getId(), 1);
        assertNotNull(pinkfloyd1);
        assertEquals(1, pinkfloyd1.getAlbums().size());
        assertNull(pinkfloyd1.getAlbums().iterator().next().getRecording());

        //Load Pink Floyd to depth -1 in a new session
        Session session2 = sessionFactory.openSession();
        Artist pinkfloyd2 = session2.load(Artist.class, pinkFloyd.getId(), -1);
        assertNotNull(pinkfloyd2);
        assertEquals(1, pinkfloyd2.getAlbums().size());
        assertNotNull(pinkfloyd2.getAlbums().iterator().next().getRecording());

        //Load Pink Floyd to depth -1 in an existing session which has loaded it to depth 1 previously
        Artist pinkfloyd_1_1 = session1.load(Artist.class, pinkFloyd.getId(), -1);
        assertNotNull(pinkfloyd_1_1);
        assertEquals(1, pinkfloyd_1_1.getAlbums().size());
        assertNotNull(pinkfloyd2.getAlbums().iterator().next().getRecording());
    }

    @Test
    public void shouldRefreshPropertiesOnEntityReload() {
        Artist pinkFloyd = new Artist("Pink Floyd");
        session.save(pinkFloyd);
        session.clear();

        //Load Pink Floyd in a new session, session1
        Session session1 = sessionFactory.openSession();
        Artist pinkfloyd1 = session1.load(Artist.class, pinkFloyd.getId(), 1);
        assertNotNull(pinkfloyd1);
        assertEquals("Pink Floyd", pinkfloyd1.getName());

        //Load Pink Floyd to in another new session, session2
        Session session2 = sessionFactory.openSession();
        Artist pinkfloyd2 = session2.load(Artist.class, pinkFloyd.getId(), -1);
        assertNotNull(pinkfloyd2);
        assertEquals("Pink Floyd", pinkfloyd2.getName());
        //update the name property
        pinkfloyd2.setName("Purple Floyd");
        //and save it in session2. Now the name in the graph is Purple Floyd
        session2.save(pinkfloyd2);

        //Reload Pink Floyd in session1
        Artist pinkfloyd_1_1 = session1.load(Artist.class, pinkFloyd.getId(), -1);
        assertNotNull(pinkfloyd_1_1);
        assertEquals("Purple Floyd", pinkfloyd_1_1.getName()); //the name should be refreshed from the graph
    }
}
