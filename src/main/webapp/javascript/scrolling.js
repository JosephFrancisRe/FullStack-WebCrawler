/*
    Functionality derived from the following blog post:
    https://blog.adam-marsden.co.uk/minimal-page-transitions-with-jquery-css-d97f692d5292

    Modifications made by Joseph Re for WebCrawler's dimensions
*/
$(function() {
    $('a[href*=#]:not([href=#])').click(function() {
        if (location.pathname.replace(/^\//,'') == this.pathname.replace(/^\//,'') && location.hostname == this.hostname) {
            var target = $(this.hash);
            target = target.length ? target : $('[name=' + this.hash.slice(1) +']');
            if (target.length) {
            $('html,body').animate({
                scrollTop: target.offset().top-70
            }, 50);
            return false;
            }
        }
    });
});