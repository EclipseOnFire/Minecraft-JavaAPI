/*
 * Copyright 2016 Arthur Mille.
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 */
package fr.eclipseonfire.mjapi.implementations.http;

import com.google.gson.annotations.SerializedName;
import fr.eclipseonfire.mjapi.implementations.http.HttpMinecraftTextures.Textures.UrlTexture;
import fr.eclipseonfire.mjapi.interfaces.MinecraftCape;
import fr.eclipseonfire.mjapi.interfaces.MinecraftSkin;
import fr.eclipseonfire.mjapi.interfaces.MinecraftTextures;
import fr.eclipseonfire.mjapi.interfaces.UuidConverter;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import javax.imageio.ImageIO;

public class HttpMinecraftTextures implements MinecraftTextures{
    
    public static final String URL_RESOURCES = "/fr/eclipseonfire/mjapi/resources/";
    
    private Long timestamp;
    private String profileId;
    private String profileName;
    private boolean isPublic;
    private Textures textures;
    
    public HttpMinecraftTextures(){}
    
    @Override
    public URL getCapeDownloadUrl(){
        return this.textures.getCape() == null ? null : this.textures.getCape().getUrl();
    }

    @Override
    public URL getSkinDownloadUrl(){
        return this.textures.getSkin() == null ? null : this.textures.getSkin().getUrl();
    }

    @Override
    public MinecraftSkin downloadSkin(Proxy proxy) throws IOException{
        URL skinUrl = this.getSkinDownloadUrl();
        
        if(skinUrl == null){
            return this.getDefaultSkin();
        }
        
        try(InputStream in = Utilities.openConnection(skinUrl, proxy).getInputStream()){
            return new MinecraftSkin(ImageIO.read(in), this.isSlim(), this.isAlex());
        }
    }

    @Override
    public MinecraftCape downloadCape(Proxy proxy) throws IOException{
        URL capeUrl = this.getCapeDownloadUrl();
        
        if(capeUrl == null){
            return null;
        }
        
        URLConnection connection = Utilities.openConnection(capeUrl, proxy);
        
        try(InputStream in = new BufferedInputStream(connection.getInputStream())){
            
            BufferedImage capeImage = ImageIO.read(in);
            
            return new MinecraftCape(capeImage);
        }
    }

    public Long getTimestamp(){
        return this.timestamp;
    }

    public String getProfileId(){
        return this.profileId;
    }

    public String getProfileName(){
        return this.profileName;
    }

    public boolean isPublic(){
        return this.isPublic;
    }
    
    private MinecraftSkin getDefaultSkin() throws IOException{
        boolean alex = this.isAlex();
        
        String url = alex ? URL_RESOURCES + "alex.png" : URL_RESOURCES + "steve.png";
        
        try(InputStream in = this.getClass().getResourceAsStream(url)){
            return new MinecraftSkin(ImageIO.read(in), this.isSlim(), alex);
        }
    }
    
    private boolean isAlex(){
        return (UuidConverter.fromMinecraftUuid(this.profileId).hashCode() & 1) != 0;
    }
    
    private boolean isSlim(){
        UrlTexture skin = this.textures.getSkin();
        
        return skin == null || skin.getMetadata() == null ? false : "slim".equals(skin.getMetadata().get("model"));
    }
    
    public class Textures{
        
        @SerializedName("SKIN")
        private UrlTexture skin;
        
        @SerializedName("CAPE")
        private UrlTexture cape;
        
        public UrlTexture getSkin(){
            return this.skin;
        }
        
        public UrlTexture getCape(){
            return this.cape;
        }
        
        public class UrlTexture{
            
            private Map<String, Object> metadata;
            private String url;
            
            public URL getUrl(){
                try{
                    return new URL(this.url);
                }
                catch(MalformedURLException ex){
                    throw new RuntimeException("Retrieved URL is invalid!", ex);
                }
            }
            
            public Map<String, Object> getMetadata(){
                return this.metadata;
            }
        }
    }
}