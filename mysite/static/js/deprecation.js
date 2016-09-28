function split(str, sep)
{
   var item = new Array();
   if(str.indexOf(sep) == -1)
       item[0] = str;
   else
   {
       var begin = 0, end = 0;
       for(var i = 0; ; i++)
       {
           end = str.indexOf(sep, begin);
           if(end == -1)
           {
               item[i] = str.substring(begin);
               break;
           }
           if(end > begin)
               item[i] = str.substring(begin, end);
           begin = end + sep.length;
       }
   }
   return item;
}