import bpy
import os
from mathutils import *
from math import *

# get Haxler environment vars
res_x = os.environ.get("HAXLER_RES_X")
res_y = os.environ.get("HAXLER_RES_Y")
frame_number = os.environ.get("HAXLER_FRAME")
samples = os.environ.get("HAXLER_SAMPLES")
project_name = os.environ.get("HAXLER_PROJECTNAME")
project_stereo = os.environ.get("HAXLER_STEREO")


use_border = os.environ.get("HAXLER_USE_BORDER")
border_parts_minx = float(os.environ.get("HAXLER_BORDER_PARTS_MINX"))
border_parts_maxx = float(os.environ.get("HAXLER_BORDER_PARTS_MAXX"))
border_parts_miny = float(os.environ.get("HAXLER_BORDER_PARTS_MINY"))
border_parts_maxy = float(os.environ.get("HAXLER_BORDER_PARTS_MAXY"))
border_parts_num = os.environ.get("HAXLER_BORDER_PARTS_NUM")



# import pdb; pdb.set_trace()
bpy.context.scene.render.engine = 'CYCLES'

if project_stereo:
    # fisheye
    bpy.context.scene.camera.data.type = 'PANO'
    bpy.context.scene.camera.data.cycles.panorama_type = 'FISHEYE_EQUISOLID'
    bpy.context.scene.camera.data.stereo.convergence_mode = 'PARALLEL'
    # bpy.context.scene.camera.data.stereo_mode = 'SIDEBYSIDE'

    scene = bpy.context.scene
    scene.render.use_multiview = True
    scene.render.views_format = 'STEREO_3D'

# resolution
if res_x:
    bpy.context.scene.render.resolution_x = int(res_x)

if res_y:
    bpy.context.scene.render.resolution_y = int(res_y)


if use_border:
    bpy.context.scene.render.use_border = True
    bpy.context.scene.render.border_min_x = border_parts_minx
    bpy.context.scene.render.border_max_x = border_parts_maxx
    bpy.context.scene.render.border_min_y = border_parts_miny
    bpy.context.scene.render.border_max_y = border_parts_maxy

# frames
bpy.context.scene.frame_start = int(frame_number)
bpy.context.scene.frame_end = int(frame_number)
bpy.context.scene.frame_step = 1

# bpy.context.scene.render.pixel_aspect_x = 1
# bpy.context.scene.render.pixel_aspect_y = 1
# bpy.context.scene.render.use_file_extension = True
bpy.context.scene.render.image_settings.color_mode = 'RGBA'
bpy.context.scene.render.image_settings.file_format = 'PNG'
bpy.context.scene.render.filepath = "%s_%sx%s_%s_#####" % (project_name,
                                                        bpy.context.scene.render.resolution_x,
                                                        bpy.context.scene.render.resolution_y,
                                                        border_parts_num
                                                           )
# bpy.context.scene.cycles.progressive = 'PATH'
if samples:
    bpy.context.scene.cycles.samples = int(samples)

bpy.context.scene.cycles.max_bounces = 4  # todo
bpy.context.scene.cycles.min_bounces = 4
bpy.context.scene.cycles.glossy_bounces = 4
bpy.context.scene.cycles.transmission_bounces = 4
bpy.context.scene.cycles.volume_bounces = 4
bpy.context.scene.cycles.transparent_max_bounces = 4
bpy.context.scene.cycles.transparent_min_bounces = 4
bpy.context.scene.render.tile_x = 64  # todo
bpy.context.scene.render.tile_y = 64

# Render results
bpy.ops.render.render(animation=True)
