# High copy WeChat image selector
Including the switch folders,dynamic record user selected pictures and preview images, etc

## Rendering
<img src="https://github.com/Leeeyou/BlogResource/blob/master/source/images/githubpages/%E9%AB%98%E4%BB%BF%E5%BE%AE%E4%BF%A1%E9%80%89%E6%8B%A9%E5%9B%BE%E7%89%87.gif?raw=true"/>

## Project description
On the basis of this Demo Zhang Hongyang blog [Android 超高仿微信图片选择器图片该这么加载](http://blog.csdn.net/lmj623565791/article/details/39943731)，Major point of modification is as follows：<br>
- 1、According to user selection dynamic display the number of the selected images<br><br>
- 2、Increase the image preview function<br>
  + 2.1：There are two pictures preview entry：①Click on the gridview image preview area (image upper right)；②Click the preview button in the lower-right corner of the window<br><br>
  + 2.2：Pictures can be zoom 、shrink in the  preview images interface<br><br>
  + 2.3: In picture preview interface , you can select and deselect images，After exit picture preview interface，"depending on the situation" dynamic refresh the selection state of the pictures in the ImageGridShowActivity interface<br><br>
  + 2.4：The above "depending on the situation" has the following two kinds：<br>
    - 2.4.1: If the user click preview images, just preview images and did not do any operation selected, deselect photoes, so will not refresh ImageGridShowActivity interface when the user exit preview image interface
    - 2.4.2：If the user go in the interface when the selected images and exit the interface is different,so in the exit  interface when dynamic refresh ImageGridShowActivity interface<br><br>
  + 2.5：In the preview interface processing OOM<br><br>
  + 2.6：To monitor physical return key, dynamic refresh ImageGridShowActivity interface<br><br>
- 3、Modify the style of the interface<br>
  + 3.1：Folder name does not display the problem
  + 3.2：File name error display problems
  + 3.3：Folders are selected by default problems
  + 3.4：The style of the folder options problems<br><br>
- 4、The division of regional and preview images selected images<br><br>
- 5、Optimize createAdapter() method, adapter created only once<br><br>
- 6、Folder and images in reverse chronological order<br><br>


[中文版](https://github.com/LeeeYou/demoimgpick/blob/master/README_CHINESE.md)
