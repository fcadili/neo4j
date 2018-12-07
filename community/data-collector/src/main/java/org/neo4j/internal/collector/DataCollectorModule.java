/*
 * Copyright (c) 2002-2018 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.internal.collector;

import org.neo4j.dbms.database.DatabaseManager;
import org.neo4j.exceptions.KernelException;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.impl.proc.Procedures;
import org.neo4j.kernel.monitoring.Monitors;
import org.neo4j.scheduler.JobScheduler;

import static java.util.Objects.requireNonNull;

public class DataCollectorModule
{
    private DataCollectorModule()
    {
    }

    public static AutoCloseable setupDataCollector( Procedures procedures,
                                                    JobScheduler jobScheduler,
                                                    DatabaseManager databaseManager,
                                                    Config config,
                                                    Monitors monitors ) throws KernelException
    {
        requireNonNull( databaseManager );
        requireNonNull( config );
        DataCollector dataCollector = new DataCollector( databaseManager, config, jobScheduler, monitors );
        procedures.registerComponent( DataCollector.class, ctx -> dataCollector, false );
        procedures.registerProcedure( DataCollectorProcedures.class );
        return dataCollector;
    }
}