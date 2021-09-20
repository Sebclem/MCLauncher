/*
 * Copyright 2015 Lifok
 *
 * This file is part of NoLogin.

 * NoLogin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NoLogin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with NoLogin.  If not, see <http://www.gnu.org/licenses/>.
 */
package McLauncher.Auth;

import java.time.LocalDateTime;

public class Account {
    private String uuid, displayName, accessToken, clientToken, refreshToken, username;
    private LocalDateTime tokenExpireDate;

    public Account(String accessToken, String refreshToken, LocalDateTime tokenExpireDate) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenExpireDate = tokenExpireDate;
    }

    public Account(String uuid, String displayName, String accessToken, String clientToken, LocalDateTime tokenExpireDate, String refreshToken, String username) {
        this.uuid = uuid;
        this.displayName = displayName;
        this.accessToken = accessToken; //Minecraft access_token
        this.username = username;
        this.clientToken = clientToken; //Microsoft access_token
        this.refreshToken = refreshToken; //For Microsoft
        this.tokenExpireDate = tokenExpireDate; //For Microsoft
    }

    public Account() {

    }


    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUUID() {
        return uuid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getClientToken() {
        return clientToken;
    }

    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public LocalDateTime getTokenExpireDate() {
        return tokenExpireDate;
    }

    public void setTokenExpireDate(LocalDateTime tokenExpireDate) {
        this.tokenExpireDate = tokenExpireDate;
    }
}