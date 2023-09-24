package com.mammoth.hueBridge;

import java.util.List;

import jakarta.validation.constraints.AssertTrue;

public class LightRequest {
	private int fadeMins;
	private List<String> lights;
	public int getFadeMins() {
		return fadeMins;
	}
	public void setFadeMins(int fadeMins) {
		this.fadeMins = fadeMins;
	}
	public List<String> getLights() {
		return lights;
	}
	public void setLights(List<String> lights) {
		this.lights = lights;
	}
	
	@AssertTrue(message = "Allowed lights are 'Nightstand' or 'StainedGlass'")
    public boolean isLightsValid() {
        if (lights == null || lights.isEmpty()) {
            return true; // No lights specified is also valid
        }

        for (String light : lights) {
            if (!"Nightstand".equals(light) && !"StainedGlass".equals(light)) {
                return false;
            }
        }

        return true;
    }
}
