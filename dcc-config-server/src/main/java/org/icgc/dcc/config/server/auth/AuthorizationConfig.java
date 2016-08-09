/*
 * Copyright (c) 2016 The Ontario Institute for Cancer Research. All rights reserved.                             
 *                                                                                                               
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with                                  
 * this program. If not, see <http://www.gnu.org/licenses/>.                                                     
 *                                                                                                               
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY                           
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES                          
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT                           
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,                                
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED                          
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;                               
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER                              
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN                         
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
// @formatter:off
package org.icgc.dcc.config.server.auth;

import static java.util.stream.Collectors.joining;

import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

/**
 * Authorization configuration.
 * 
 * @see https://github.com/spring-cloud/spring-cloud-config/issues/464
 */
@Slf4j
@Configuration
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class AuthorizationConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  AuthProperties properties;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    // Allow home page for all
    http
        .authorizeRequests()
          .antMatchers("/").permitAll();

    // From most restrictive match to least
    for (val name : properties.getNames())
      for (val profile : properties.getProfiles())
        authorizeConfigRequest(http, name, profile);
    for (val name : properties.getNames())
      authorizeConfigRequest(http, name, "*");
    for (val profile : properties.getProfiles())
      authorizeConfigRequest(http, "*", profile);

    // Require basic authentication
    http
        .authorizeRequests()
          .anyRequest().authenticated()
        .and()
          .httpBasic();
  }

  @SneakyThrows
  private static void authorizeConfigRequest(HttpSecurity http, String name, String profile) {
    boolean exact = !name.equals("*") && !profile.equals("*");

    // From org.springframework.cloud.config.server.EnvironmentController
    String[] patterns = { // 
        "/" + name + "/" + profile + "/*/*",
        
        "/" + name + "/" + profile + "/*",
        "/" + name + "-" + profile + ".properties",
        "/" + name + "-" + profile + ".json",
        "/" + name + "-" + profile + ".yaml",
        "/" + name + "-" + profile + ".yml",
        
        "/*/" + name + "-" + profile + ".properties",
        "/*/" + name + "-" + profile + ".json",
        "/*/" + name + "-" + profile + ".yaml",
        "/*/" + name + "-" + profile + ".yml"
    };

    val access = any(
                hasAuthorities("name:*",       "profile:*"),
        exact ? hasAuthorities("name:" + name, "profile:*")          : null,
        exact ? hasAuthorities("name:*",       "profile:" + profile) : null,
                hasAuthorities("name:" + name, "profile:" + profile));

    log.info("Authorizing '{}' with: {}", patterns, access);
    http.authorizeRequests().antMatchers(patterns).access(access);
  }

  private static String any(String... expressions) {
    return Stream.of(expressions)
        .filter(e -> e != null)
        .map(e -> "(" + e + ")")
        .collect(joining(" || "));
  }

  private static String hasAuthorities(String... authorities) {
    return Stream.of(authorities)
        .map(a -> "hasAuthority('" + a + "')")
        .collect(joining(" && "));
  }

}
