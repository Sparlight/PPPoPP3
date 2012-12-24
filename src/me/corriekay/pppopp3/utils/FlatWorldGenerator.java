package me.corriekay.pppopp3.utils;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

public class FlatWorldGenerator extends ChunkGenerator{

	public FlatWorldGenerator(){

	}

	void setBlock(byte[][] result, int x, int y, int z, byte blkid){
		if(result[y >> 4] == null) {
			result[y >> 4] = new byte[4096];
		}
		result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = blkid; // Look into
																	// the
																	// tutorial
																	// ;)
	}

	@Override
	public byte[][] generateBlockSections(World world, Random random, int chunkX, int chunkZ, BiomeGrid biomeGrid){
		byte[][] result = new byte[world.getMaxHeight() / 16][];
		int x, z;

		// Sets bedrock layer
		for(x = 0; x < 16; x++) {
			for(z = 0; z < 16; z++) {
				setBlock(result, x, 0, z, (byte)Material.BEDROCK.getId());
			}
		}

		// Sets the dirt layers
		for(x = 0; x < 16; x++) {
			for(z = 0; z < 16; z++) {
				setBlock(result, x, 1, z, (byte)Material.GRASS.getId());
			}
		}

		return result;
	}
}