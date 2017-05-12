/*
 * ****************************************************************************
 *     Cloud Foundry
 *     Copyright (c) [2009-2016] Pivotal Software, Inc. All Rights Reserved.
 *
 *     This product is licensed to you under the Apache License, Version 2.0 (the "License").
 *     You may not use this product except in compliance with the License.
 *
 *     This product includes a number of subcomponents with
 *     separate copyright notices and license terms. Your use of these
 *     subcomponents is subject to the terms and conditions of the
 *     subcomponent's license, as noted in the LICENSE file.
 * ****************************************************************************
 */

package org.cloudfoundry.identity.uaa.zone.event;

import org.cloudfoundry.identity.uaa.config.IdentityZoneConfiguration;
import org.cloudfoundry.identity.uaa.zone.IdentityZone;
import org.cloudfoundry.identity.uaa.zone.IdentityZoneHolder;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.Assert.assertEquals;


public class IdentityZoneModifiedEventTest {

    private IdentityZone zone;

    @Before
    public void setup() {
        IdentityZoneHolder.clear();
        SecurityContextHolder.clearContext();
        zone = new IdentityZone();
        zone.setId("id");
        zone.setSubdomain("subdomain");
        zone.setName("Test Zone");
        zone.setDescription("Test Zone Description");
        zone.setConfig(new IdentityZoneConfiguration());
        zone.getConfig().getSamlConfig().setPrivateKey("key");
        zone.getConfig().getSamlConfig().setCertificate("certificate");
    }

    @Test
    public void identityZoneCreated() throws Exception {
        evaluteZoneAuditData(IdentityZoneModifiedEvent.identityZoneCreated(zone));
    }

    @Test
    public void identityZoneModified() throws Exception {
        evaluteZoneAuditData(IdentityZoneModifiedEvent.identityZoneModified(zone));
    }

    public void evaluteZoneAuditData(IdentityZoneModifiedEvent event) {
        String s = event.getAuditEvent().getData();
        assertEquals(String.format(IdentityZoneModifiedEvent.dataFormat,
                                   zone.getId(),
                                   zone.getSubdomain()),
                     s);
    }

}