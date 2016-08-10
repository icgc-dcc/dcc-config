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
package org.icgc.dcc.config.server.auth;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.ToString;

/**
 * Authorization and authentication properties.
 */
@Data
@Component
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

  /**
   * The explicit list of applications supported by this server.
   */
  List<String> applications = newArrayList();

  /**
   * The explicit list of applications supported by this server.
   */
  List<String> profiles = newArrayList();

  /**
   * The authorized users.
   */
  List<User> users = newArrayList();

  @Data
  @ToString(exclude = "password")
  public static class User {

    /**
     * The username used authenticate with the server
     */
    String username;

    /**
     * The password used authenticate with the server
     */
    String password;

    /**
     * The set of authorities given to this user as defined by the system.
     * <p>
     * e.g. {@literal application:*}, {@literal profile:*}, any {@literal application:<application>} in
     * {@link AuthProperties#applications} or {@literal profile:<profile>} in {@link AuthProperties#profiles}
     */
    String[] authorities = new String[] {};

  }

}
