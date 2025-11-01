#!/usr/bin/env python3
"""
Block Texture Tinting Script
============================

This script creates cyan, magenta, and yellow tinted versions of block textures
while preserving the original marble texture and intensity.

Usage:
    python texture_tinter.py [input_directory] [output_directory]

Requirements:
    pip install Pillow numpy

Features:
- Preserves marble texture and detail
- Maintains original intensity levels
- Creates organized output structure
- Supports PNG with transparency
- Batch processes multiple textures
"""

import os
import sys
import argparse
from pathlib import Path
from PIL import Image, ImageEnhance, ImageChops
import numpy as np

class TextureTinter:
    """Advanced texture tinting with marble texture preservation"""
    
    def __init__(self):
        # Tint colors (RGB values)
        self.tints = {
            'cyan': (0, 255, 255),
            'magenta': (255, 0, 255), 
            'yellow': (255, 255, 0)
        }
        
        # Tinting parameters
        self.tint_strength = 0.4  # 40% blend for natural look
        self.preserve_highlights = True
        self.preserve_shadows = True
        
    def analyze_texture_intensity(self, image):
        """Analyze the original texture to maintain its intensity characteristics"""
        # Convert to grayscale to analyze luminosity
        gray = image.convert('L')
        
        # Get intensity statistics
        pixels = np.array(gray)
        
        return {
            'mean_brightness': np.mean(pixels),
            'std_deviation': np.std(pixels),
            'min_brightness': np.min(pixels),
            'max_brightness': np.max(pixels),
            'contrast_ratio': (np.max(pixels) - np.min(pixels)) / 255.0
        }
    
    def preserve_marble_texture(self, original, tinted):
        """Preserve the marble texture details while applying tint"""
        # Convert images to arrays for processing
        orig_array = np.array(original.convert('RGB'))
        tint_array = np.array(tinted.convert('RGB'))
        
        # Calculate luminosity of original
        orig_lum = np.dot(orig_array, [0.299, 0.587, 0.114])
        
        # Create a detail mask based on texture variation
        detail_mask = np.abs(orig_lum - np.mean(orig_lum))
        detail_mask = (detail_mask / np.max(detail_mask)) if np.max(detail_mask) > 0 else detail_mask
        
        # Blend based on texture detail - preserve more detail in textured areas
        blend_factor = 0.3 + (detail_mask * 0.4)  # 30-70% original based on texture
        
        # Apply per-pixel blending
        result_array = np.zeros_like(orig_array)
        for i in range(3):  # RGB channels
            result_array[:,:,i] = (
                orig_array[:,:,i] * blend_factor + 
                tint_array[:,:,i] * (1 - blend_factor)
            )
        
        return Image.fromarray(result_array.astype(np.uint8))
    
    def apply_smart_tint(self, image, tint_color, intensity_info):
        """Apply tint while preserving texture characteristics"""
        if image.mode == 'RGBA':
            # Separate RGB and alpha channels
            rgb_img = Image.new('RGB', image.size, (255, 255, 255))
            rgb_img.paste(image, mask=image.split()[3])  # Use alpha as mask
            alpha = image.split()[3]
        else:
            rgb_img = image.convert('RGB')
            alpha = None
        
        # Create tint overlay
        tint_overlay = Image.new('RGB', rgb_img.size, tint_color)
        
        # Apply different blending based on brightness regions
        gray = rgb_img.convert('L')
        gray_array = np.array(gray)
        
        # Create masks for different brightness regions
        highlights = gray_array > (intensity_info['mean_brightness'] + intensity_info['std_deviation'])
        shadows = gray_array < (intensity_info['mean_brightness'] - intensity_info['std_deviation'])
        midtones = ~(highlights | shadows)
        
        # Blend with different strengths for different regions
        if self.preserve_highlights:
            # Lighter tint on highlights to preserve marble shine
            highlight_blend = ImageChops.blend(rgb_img, tint_overlay, self.tint_strength * 0.6)
        else:
            highlight_blend = ImageChops.blend(rgb_img, tint_overlay, self.tint_strength)
            
        if self.preserve_shadows:
            # Stronger tint on shadows to enhance depth
            shadow_blend = ImageChops.blend(rgb_img, tint_overlay, self.tint_strength * 1.2)
        else:
            shadow_blend = ImageChops.blend(rgb_img, tint_overlay, self.tint_strength)
        
        # Normal tint on midtones
        midtone_blend = ImageChops.blend(rgb_img, tint_overlay, self.tint_strength)
        
        # Combine the different regions
        result_array = np.array(rgb_img)
        highlight_array = np.array(highlight_blend)
        shadow_array = np.array(shadow_blend)
        midtone_array = np.array(midtone_blend)
        
        # Apply region-specific tinting
        result_array[highlights] = highlight_array[highlights]
        result_array[shadows] = shadow_array[shadows]
        result_array[midtones] = midtone_array[midtones]
        
        result_img = Image.fromarray(result_array)
        
        # Preserve marble texture details
        final_img = self.preserve_marble_texture(rgb_img, result_img)
        
        # Restore alpha channel if present
        if alpha is not None:
            final_img = final_img.convert('RGBA')
            final_img.putalpha(alpha)
        
        return final_img
    
    def enhance_contrast_preservation(self, original, tinted, intensity_info):
        """Ensure the tinted version maintains the original's contrast characteristics"""
        # Analyze contrast in both images
        orig_enhancer = ImageEnhance.Contrast(original)
        tint_enhancer = ImageEnhance.Contrast(tinted)
        
        # Calculate contrast adjustment needed
        orig_contrast = intensity_info['contrast_ratio']
        
        # Adjust tinted image to match original contrast
        if orig_contrast > 0.5:  # High contrast textures
            contrast_factor = 1.0 + (orig_contrast - 0.5) * 0.3
            tinted = tint_enhancer.enhance(contrast_factor)
        
        return tinted
    
    def process_texture(self, input_path, output_dir):
        """Process a single texture file"""
        try:
            # Load original image
            original = Image.open(input_path)
            
            print(f"Processing: {input_path.name}")
            print(f"  Size: {original.size}")
            print(f"  Mode: {original.mode}")
            
            # Analyze texture intensity
            intensity_info = self.analyze_texture_intensity(original)
            print(f"  Brightness: {intensity_info['mean_brightness']:.1f}")
            print(f"  Contrast: {intensity_info['contrast_ratio']:.2f}")
            
            # Create tinted versions
            for tint_name, tint_color in self.tints.items():
                print(f"  Creating {tint_name} version...")
                
                # Apply smart tint
                tinted = self.apply_smart_tint(original, tint_color, intensity_info)
                
                # Enhance contrast preservation
                tinted = self.enhance_contrast_preservation(original, tinted, intensity_info)
                
                # Generate output filename
                stem = input_path.stem
                output_filename = f"{stem}_{tint_name}.png"
                output_path = output_dir / output_filename
                
                # Save as PNG with optimization
                if tinted.mode == 'RGBA':
                    tinted.save(output_path, 'PNG', optimize=True)
                else:
                    tinted.save(output_path, 'PNG', optimize=True)
                
                print(f"    Saved: {output_filename}")
            
            print(f"  ✓ Complete: {input_path.name}\\n")
            return True
            
        except Exception as e:
            print(f"  ✗ Error processing {input_path.name}: {e}\\n")
            return False
    
    def process_directory(self, input_dir, output_dir):
        """Process all texture files in a directory"""
        input_path = Path(input_dir)
        output_path = Path(output_dir)
        
        # Create output directory structure
        output_path.mkdir(parents=True, exist_ok=True)
        
        # Create subdirectories for each tint
        for tint_name in self.tints.keys():
            (output_path / tint_name).mkdir(exist_ok=True)
        
        # Find all PNG files
        texture_files = list(input_path.glob('*.png'))
        
        if not texture_files:
            print(f"No PNG files found in {input_dir}")
            return
        
        print(f"Found {len(texture_files)} texture files to process\\n")
        
        # Process each file
        successful = 0
        for texture_file in texture_files:
            if self.process_texture(texture_file, output_path):
                successful += 1
        
        print(f"\\n=== PROCESSING COMPLETE ===")
        print(f"Successfully processed: {successful}/{len(texture_files)} files")
        print(f"Output directory: {output_path}")
        
        # Create organized output structure
        self.organize_outputs(output_path)
    
    def organize_outputs(self, output_path):
        """Organize tinted textures into color-specific subdirectories"""
        print("\\nOrganizing tinted textures...")
        
        for tint_name in self.tints.keys():
            tint_dir = output_path / tint_name
            tint_dir.mkdir(exist_ok=True)
            
            # Move tinted files to appropriate subdirectories
            for file in output_path.glob(f'*_{tint_name}.png'):
                new_path = tint_dir / file.name.replace(f'_{tint_name}', '')
                file.rename(new_path)
                print(f"  Moved: {file.name} → {tint_name}/{new_path.name}")
        
        print("✓ Organization complete!")

def main():
    parser = argparse.ArgumentParser(
        description='Create cyan, magenta, and yellow tinted versions of block textures',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog='''
Examples:
  python texture_tinter.py textures/ output/
  python texture_tinter.py ./my_blocks ./tinted_blocks
  python texture_tinter.py  # Uses current directory

The script will create three subdirectories in the output folder:
  - cyan/     (cyan tinted textures)
  - magenta/  (magenta tinted textures)  
  - yellow/   (yellow tinted textures)
        '''
    )
    
    parser.add_argument(
        'input_dir',
        nargs='?',
        default='.',
        help='Input directory containing PNG texture files (default: current directory)'
    )
    
    parser.add_argument(
        'output_dir', 
        nargs='?',
        default='tinted_textures',
        help='Output directory for tinted textures (default: ./tinted_textures)'
    )
    
    parser.add_argument(
        '--tint-strength',
        type=float,
        default=0.4,
        help='Tint strength (0.0-1.0, default: 0.4)'
    )
    
    parser.add_argument(
        '--preserve-highlights',
        action='store_true',
        default=True,
        help='Preserve highlights in marble texture (default: enabled)'
    )
    
    parser.add_argument(
        '--preserve-shadows',
        action='store_true', 
        default=True,
        help='Preserve shadows in marble texture (default: enabled)'
    )
    
    args = parser.parse_args()
    
    # Validate input directory
    if not Path(args.input_dir).exists():
        print(f"Error: Input directory '{args.input_dir}' does not exist")
        sys.exit(1)
    
    # Create tinter instance
    tinter = TextureTinter()
    tinter.tint_strength = args.tint_strength
    tinter.preserve_highlights = args.preserve_highlights
    tinter.preserve_shadows = args.preserve_shadows
    
    # Display configuration
    print("=== TEXTURE TINTING SCRIPT ===")
    print(f"Input directory: {args.input_dir}")
    print(f"Output directory: {args.output_dir}")
    print(f"Tint strength: {args.tint_strength}")
    print(f"Preserve highlights: {args.preserve_highlights}")
    print(f"Preserve shadows: {args.preserve_shadows}")
    print("="*50)
    print()
    
    # Process textures
    tinter.process_directory(args.input_dir, args.output_dir)

if __name__ == '__main__':
    main()