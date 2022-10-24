from operator import mod
import re
import urllib.request

"""
将 markdown 文件中的 图片引用全部下载到本地 并替换其中的链接
"""
file_in_name="JavaNIO_2.md"
file_out_name="JavaNIO_2_1.md"
new_image_path_prefix="img/netty/"
obj = re.compile(r"\!\[(?P<image_id>.*?)\]\((?P<url>.*?)\)", re.S)
with open(file=file_in_name, mode="rt", encoding="utf-8") as fin:
    with open(file=file_out_name, mode="wt", encoding="utf-8") as fout:
        count = 0
        for line in fin:
            for it in obj.finditer(line):
                image_id = it.group("image_id")
                url = it.group("url")
                count += 1
                new_image_name = new_image_path_prefix + str(count) + ".jpg"
                urllib.request.urlretrieve(url, new_image_name)
                line = line.replace(image_id, "image-" + str(count)).replace(url, new_image_name)
            fout.write(line)
