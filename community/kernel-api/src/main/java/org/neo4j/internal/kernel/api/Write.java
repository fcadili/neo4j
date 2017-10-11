/*
 * Copyright (c) 2002-2017 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
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
package org.neo4j.internal.kernel.api;

import org.neo4j.values.storable.Value;

/**
 * Defines the write operations of the Kernel API.
 */
public interface Write
{
    long nodeCreate();

    void nodeDelete( long node );

    long relationshipCreate( long sourceNode, int relationshipLabel, long targetNode );

    void relationshipDelete( long relationship );

    void nodeAddLabel( long node, int nodeLabel );

    void nodeRemoveLabel( long node, int nodeLabel );

    Value nodeSetProperty( long node, int propertyKey, Value value );

    Value nodeRemoveProperty( long node, int propertyKey );

    Value relationshipSetProperty( long relationship, int propertyKey, Value value );

    Value relationshipRemoveProperty( long node, int propertyKey );

    Value graphSetProperty( int propertyKey, Value value );

    Value graphRemoveProperty( int propertyKey );
}
