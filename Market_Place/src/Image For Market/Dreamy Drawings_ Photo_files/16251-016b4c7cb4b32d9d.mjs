"use strict";(self.__LOADABLE_LOADED_CHUNKS__=self.__LOADABLE_LOADED_CHUNKS__||[]).push([[16251],{720687:(e,t,n)=>{n.d(t,{default:()=>r});var i=n(934980);let a=`pulsing {
  0% {
    opacity: 1;
  }

  50% {
    opacity: 0.4;
  }

  100% {
    opacity: 1;
  }
}`,r={css:(0,i.Ll)([a]),animation:"pulsing 2s infinite"}},934980:(e,t,n)=>{n.d(t,{CC:()=>a,Ll:()=>o,XF:()=>r});let i=(e,t,n)=>({x:Math.floor(e*Math.cos(n)),y:Math.floor(t*Math.sin(n))}),a=(e,t)=>i(t/2,e/2,2*Math.random()*Math.PI),r=(e,t)=>Math.floor(Math.random()*(t-e+1))+e,o=e=>["@-webkit-keyframes","@keyframes"].map(t=>e.map(e=>t+" "+e).join("\n")).join("\n")},31723:(e,t,n)=>{n.r(t),n.d(t,{default:()=>j});var i=n(667294),a=n(883119),r=n(573706),o=n(986782);function s(e,t,n){var i;return(t="symbol"==typeof(i=function(e,t){if("object"!=typeof e||!e)return e;var n=e[Symbol.toPrimitive];if(void 0!==n){var i=n.call(e,t||"default");if("object"!=typeof i)return i;throw TypeError("@@toPrimitive must return a primitive value.")}return("string"===t?String:Number)(e)}(t,"string"))?i:i+"")in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}let l={},m=e=>{let t=e.__id||e.id;return"string"==typeof t&&t||null};class u{constructor(){s(this,"idMap",new Map),s(this,"objMap",new WeakMap)}get(e){let t=m(e);return this.objMap.get(e)??(t?this.idMap.get(t):void 0)}has(e){let t=m(e);return this.objMap.has(e)??(!!t&&this.idMap.has(t))}set(e,t){let n=m(e);n&&this.idMap.set(n,t),this.objMap.set(e,t)}reset(){this.idMap=new Map,this.objMap=new WeakMap}}function p(e,t){return"number"==typeof e?e:"_lg1"===t?e[t]??e.lg??1:e[t]??1}var d=n(587435),c=n(39260),h=n(876594),g=n(720687),y=n(512541),_=n(785893);let{css:f,animation:b}=g.default,x={backgroundColor:h._VP,animation:b,borderRadius:h.Ev2};function w({data:e}){let{height:t}=e;return(0,_.jsxs)(i.Fragment,{children:[(0,_.jsx)(y.Z,{unsafeCSS:f}),(0,_.jsx)(a.xu,{dangerouslySetInlineStyle:{__style:x},"data-test-id":"skeleton-pin",children:(0,_.jsx)(a.xu,{height:t})})]})}var v=n(679482),M=n(297728),C=n(730212),k=n(410150),S=n(415787),$=n(855746);function j(e){let t,{align:n,cacheKey:s,id:m,isFetching:h,isGridCentered:g=!0,items:f,layout:b,loadItems:x,masonryRef:j,optOutFluidGridExperiment:A=!1,renderItem:E,scrollContainerRef:R,virtualize:I=!0,_getColumnSpanConfig:W,_getResponsiveModuleConfigForSecondItem:G,_dynamicHeights:F,useLoadingState:B,initialLoadingStatePinCount:P,isLoadingAccessibilityLabel:O,isLoadedAccessibilityLabel:z}=e,H=(0,k.ZP)(),{isAuthenticated:N,isRTL:L}=(0,C.B)(),{logContextEvent:T}=(0,r.v)(),X=(0,M.F)(),D="desktop"===H,Z=(0,$.MM)(),Q=((0,i.useRef)(f.map(()=>({fetchTimestamp:Date.now(),measureTimestamp:Date.now(),hasRendered:!1,pageCount:0}))),D&&!A),{experimentalColumnWidth:V,experimentalGutter:q}=(0,d.Z)(Q),K=e.serverRender??!!D,U="flexible"===b||"uniformRowFlexible"===b||"desktop"!==H||Q,J=(U&&b?.startsWith("uniformRow")?"uniformRowFlexible":void 0)??(K?"serverRenderedFlexible":"flexible"),Y=e.columnWidth??V??v.yF;U&&(Y=Math.floor(Y));let ee=e.gutterWidth??q??(D?v.oX:1),et=e.minCols??v.yc,en=(0,i.useRef)(0),ei=Y+ee,ea=function(e){if(null==e)return;let t=function(e){let t=l[e];return t&&t.screenWidth===window.innerWidth||(l[e]={screenWidth:window.innerWidth}),l[e]}(e);return t.measurementCache||(t.measurementCache=new u),t.measurementCache}(s),er=(0,i.useCallback)(()=>R?.current||window,[R]),eo=(0,i.useRef)(!0),{anyEnabled:es,group:el}=X.checkExperiment("web_masonry_enable_dynamic_heights_for_all"),{anyEnabled:em}=X.checkExperiment("web_masonry_pin_overlap_calculation_and_logging"),{anyEnabled:eu,group:ep}=X.checkExperiment("web_masonry_dynamic_batches");eu&&(t=(e,t)=>{let n={itemsBatchSize:0,whitespaceThreshold:0,iterationsLimit:15e3};return t>3&&(n.whitespaceThreshold=ee*t),"enabled_sections_position_fixed"===ep&&(n.itemsBatchSize=5,n.iterationsLimit=1e4),"enabled_sections_position_responsive"===ep&&(n.iterationsLimit=15e3,n.itemsBatchSize=5,t>5&&(n.itemsBatchSize=7)),("enabled_small_batch"===ep||"employees"===ep)&&(e>=7?n.itemsBatchSize=7:n.itemsBatchSize=5),"enabled_large_batch"===ep&&(e>=7?n.itemsBatchSize=9:n.itemsBatchSize=5),n});let ed=g&&eo.current?"centered":"",{className:ec,styles:eh}=function(e){let t=`m_${Object.keys(e).sort().reduce((t,n)=>{let i=e[n];return null==i||"object"==typeof i||"function"==typeof i?t:"boolean"==typeof i?t+(i?"t":"f"):t+i},"").replace(/\:/g,"\\:")}`,{flexible:n,gutterWidth:i,isRTL:a,itemWidth:r,maxColumns:o,minColumns:s,items:l,_getColumnSpanConfig:m,_getResponsiveModuleConfigForSecondItem:u}=e,d=m?l.map((e,t)=>({index:t,columnSpanConfig:m(e)??1})).filter(e=>1!==e.columnSpanConfig):[],c=r+i,h=Array.from({length:o+1-s},(e,t)=>t+s).map(e=>{let h,g,y=e===s?0:e*c,_=e===o?null:(e+1)*c-.01;m&&u&&l.length>1&&(h=m(l[0]),g=u(l[1]));let{styles:f,numberOfVisibleItems:b}=d.reduce((a,o)=>{let{columnSpanConfig:s}=o,m=Math.min(function({columnCount:e,columnSpanConfig:t,firstItemColumnSpanConfig:n,isFlexibleWidthItem:i,secondItemResponsiveModuleConfig:a}){let r=e<=2?"sm":e<=4?"md":e<=6?"_lg1":e<=8?"lg":"xl",o=p(t,r);if(i){let t=p(n,r);o="number"==typeof a?a:a?Math.max(a.min,Math.min(a.max,e-t)):1}return o}({columnCount:e,columnSpanConfig:s,isFlexibleWidthItem:!!g&&o===l[1],firstItemColumnSpanConfig:h??1,secondItemResponsiveModuleConfig:g??1}),e),u=null!=o.index&&a.numberOfVisibleItems>=m+o.index,d=n?100/e*m:r*m+i*(m-1),{numberOfVisibleItems:c}=a;return u?c-=m-1:o.index<c&&(c+=1),{styles:a.styles.concat(function({className:e,index:t,columnSpanConfig:n,visible:i,width:a,flexible:r}){let o="number"==typeof n?n:btoa(JSON.stringify(n));return r?`
      .${e} .static[data-column-span="${o}"]:nth-child(${t+1}) {
        visibility: ${i?"visible":"hidden"} !important;
        position: ${i?"inherit":"absolute"} !important;
        width: ${a}% !important;
      }`:`
      .${e} .static[data-column-span="${o}"]:nth-child(${t+1}) {
        visibility: ${i?"visible":"hidden"} !important;
        position: ${i?"inherit":"absolute"} !important;
        width: ${a}px !important;
      }`}({className:t,index:o.index,columnSpanConfig:s,visible:u,width:d,flexible:n})),numberOfVisibleItems:c}},{styles:"",numberOfVisibleItems:e}),x=n?`
      .${t} .static {
        box-sizing: border-box;
        width: calc(100% / ${e}) !important;
      }
    `:`
      .${t} {
        max-width: ${e*c}px;
      }

      .${t} .static {
        width: ${r}px !important;
      }
    `;return{minWidth:y,maxWidth:_,styles:`
      .${t} .static:nth-child(-n+${b}) {
        position: static !important;
        visibility: visible !important;
        float: ${a?"right":"left"};
        display: block;
      }

      .${t} .static {
        padding: 0 ${i/2}px;
      }

      ${x}

      ${f}
    `}}),g=h.map(({minWidth:e,maxWidth:t,styles:n})=>`
    @container (min-width: ${e}px) ${t?`and (max-width: ${t}px)`:""} {
      ${n}
    }
  `),y=h.map(({minWidth:e,maxWidth:t,styles:n})=>`
    @media (min-width: ${e}px) ${t?`and (max-width: ${t}px)`:""} {
      ${n}
    }
  `),_=`
    ${g.join("")}
    @supports not (container-type: inline-size) {
      ${y.join("")}
    }
  `;return{className:t,styles:`
    .masonryContainer:has(.${t}) {
      container-type: inline-size;
    }

    .masonryContainer > .centered {
      margin-left: auto;
      margin-right: auto;
    }

    .${t} .static {
      position: absolute !important;
      visibility: hidden !important;
    }

    ${_}
  `}}({gutterWidth:ee,flexible:U,items:f,isRTL:L,itemWidth:Y,maxColumns:e.maxColumns??Math.max(f.length,v.g5),minColumns:et,_getColumnSpanConfig:W,_getResponsiveModuleConfigForSecondItem:G}),eg=`${ed} ${ec}`.trim(),{anyEnabled:ey,expName:e_,group:ef,isMeasureAllEnabled:eb}=(0,c.Z)(),ex=((0,i.useRef)(void 0),(0,i.useRef)(f.length)),ew=(0,i.useRef)(0),ev=(0,i.useRef)(null);(0,i.useEffect)(()=>{ex.current=f.length,ew.current+=1},[f]),(0,i.useEffect)(()=>{eo.current&&(eo.current=!1)},[]),(0,i.useEffect)(()=>()=>{},[]);let eM=(0,i.useCallback)((e,t,n)=>{let i=e.reduce((e,t)=>e+t),a=i/e.length;(0,S.S0)("webapp.masonry.multiColumnWhitespace.average",a,{sampleRate:1,tags:{experimentalMasonryGroup:ef||"unknown",dynamicBatchesExperimentGroup:ep||"unknown",handlerId:Z,isAuthenticated:N,multiColumnItemSpan:e.length}}),(0,S.S0)("webapp.masonry.twoColWhitespace",a,{sampleRate:1,tags:{columnWidth:Y,minCols:et}}),ep&&(0,S.S0)("webapp.masonry.graphIterations",t,{sampleRate:1,tags:{columnSpan:n,experimentGroup:ep}}),T({event_type:15878,component:14468,aux_data:{total_whitespace_px:i}}),T({event_type:16062,component:14468,aux_data:{average_whitespace_px:a}}),T({event_type:16063,component:14468,aux_data:{max_whitespace_px:Math.max(...e)}}),e.forEach(t=>{t>=50&&((0,S.nP)("webapp.masonry.multiColumnWhitespace.over50",{sampleRate:1,tags:{experimentalMasonryGroup:ef||"unknown",dynamicBatchesExperimentGroup:ep||"unknown",handlerId:Z,isAuthenticated:N,multiColumnItemSpan:e.length}}),T({event_type:16261,component:14468})),t>=100&&((0,S.nP)("webapp.masonry.multiColumnWhitespace.over100",{sampleRate:1,tags:{experimentalMasonryGroup:ef||"unknown",dynamicBatchesExperimentGroup:ep||"unknown",handlerId:Z,isAuthenticated:N,multiColumnItemSpan:e.length}}),T({event_type:16262,component:14468}))}),(0,S.nP)("webapp.masonry.multiColumnWhitespace.count",{sampleRate:1,tags:{experimentalMasonryGroup:ef||"unknown",dynamicBatchesExperimentGroup:ep||"unknown",handlerId:Z,isAuthenticated:N,multiColumnItemSpan:e.length}})},[Y,T,et,N,Z,ef,ep]),{_items:eC,_renderItem:ek}=function({initialLoadingStatePinCount:e=50,infiniteScrollPinCount:t=10,isFetching:n,items:a=[],renderItem:r,useLoadingState:o}){let s=a.filter(e=>"object"==typeof e&&null!==e&&"type"in e&&"closeup_module"===e.type).length>0,l=o&&n&&0===a.length,m=o&&n&&s&&1===a.length,u=o&&n&&a.length>+!!s,p=(0,i.useMemo)(()=>Array.from({length:u?t:e}).reduce((e,t,n)=>[...e,{height:n%2==0?356:236,key:`skeleton-pin-${n}`,isSkeleton:!0}],[]),[e,t,u]);return{_items:(0,i.useMemo)(()=>m||u?[...a,...p]:l?p:a,[l,u,m,a,p]),_renderItem:(0,i.useMemo)(()=>o?e=>{let{itemIdx:t,data:n}=e;return t>=a.length&&n&&"object"==typeof n&&"key"in n&&"height"in n?(0,_.jsx)(w,{data:n},n.key):r(e)}:r,[o,r,a.length])}}({useLoadingState:B,items:f,renderItem:(0,i.useCallback)(e=>(0,_.jsx)(o.Z,{name:"MasonryItem",children:E(e)}),[E]),isFetching:h,initialLoadingStatePinCount:P}),eS=B&&h,e$=(0,i.useRef)(new Set);return(0,i.useEffect)(()=>{if(!em)return;let e=setTimeout(()=>{requestAnimationFrame(()=>{let e=Array.from(ev.current?.querySelectorAll("[data-grid-item-idx]")??[]);if(0===e.length)return;let t=e.map(e=>{let t=e.getAttribute("data-grid-item-idx");return{rect:e.getBoundingClientRect(),itemIdx:t}}),n=0,i=0,a=0,r=0,o=0,s=0;for(let e=0;e<t.length;e+=1){let l=t[e]?.rect,m=t[e]?.itemIdx;for(let u=e+1;u<t.length;u+=1){let e=t[u]?.rect,p=t[u]?.itemIdx;if(l&&e&&m&&p){let t=[m,p].sort().join("|");if(!e$.current.has(t)&&l.right>=e.left&&l.left<=e.right&&l.bottom>=e.top&&l.top<=e.bottom&&l.height>0&&e.height>0){e$.current.add(t),n+=1;let m=Math.max(0,Math.min(l.right,e.right)-Math.max(l.left,e.left))*Math.max(0,Math.min(l.bottom,e.bottom)-Math.max(l.top,e.top));m>8e4?s+=1:m>4e4?o+=1:m>1e4?r+=1:m>5e3?a+=1:m>2500&&(i+=1)}}}}n>0&&(0,S.QX)("webapp.masonry.pinOverlapHits",n,{tags:{isAuthenticated:N,isDesktop:D,handlerId:Z,experimentalMasonryGroup:ef||"unknown",dynamicHeightsForAllGroup:el||"unknown"}}),i>0&&(0,S.QX)("webapp.masonry.pinOverlap.AreaPx.over2500",i,{tags:{isAuthenticated:N,isDesktop:D,handlerId:Z,experimentalMasonryGroup:ef||"unknown",dynamicHeightsForAllGroup:el||"unknown"}}),a>0&&(0,S.QX)("webapp.masonry.pinOverlap.AreaPx.over5000",a,{tags:{isAuthenticated:N,isDesktop:D,handlerId:Z,experimentalMasonryGroup:ef||"unknown",dynamicHeightsForAllGroup:el||"unknown"}}),r>0&&(0,S.QX)("webapp.masonry.pinOverlap.AreaPx.over10000",r,{tags:{isAuthenticated:N,isDesktop:D,handlerId:Z,experimentalMasonryGroup:ef||"unknown",dynamicHeightsForAllGroup:el||"unknown"}}),o>0&&(0,S.QX)("webapp.masonry.pinOverlap.AreaPx.over40000",o,{tags:{isAuthenticated:N,isDesktop:D,handlerId:Z,experimentalMasonryGroup:ef||"unknown",dynamicHeightsForAllGroup:el||"unknown"}}),s>0&&(0,S.QX)("webapp.masonry.pinOverlap.AreaPx.over80000",s,{tags:{isAuthenticated:N,isDesktop:D,handlerId:Z,experimentalMasonryGroup:ef||"unknown",dynamicHeightsForAllGroup:el||"unknown"}})})},1e3);return()=>{clearTimeout(e)}},[Y,ef,N,D,em,f,Z,el]),(0,_.jsxs)(i.Fragment,{children:[B&&!eo.current&&(0,_.jsx)(a.xu,{"aria-live":"polite",display:"visuallyHidden",children:eS?O:z}),(0,_.jsx)("div",{ref:ev,"aria-busy":B?!!eS:void 0,className:"masonryContainer","data-test-id":"masonry-container",id:m,style:Q?{padding:`0 ${ee/2}px`}:void 0,children:(0,_.jsxs)("div",{className:eg,children:[K&&eo.current?(0,_.jsx)(y.Z,{"data-test-id":"masonry-ssr-styles",unsafeCSS:eh}):null,(0,_.jsx)(a.xu,{"data-test-id":"max-width-container",marginBottom:0,marginEnd:"auto",marginStart:"auto",marginTop:0,maxWidth:e.maxColumns?ei*e.maxColumns:void 0,children:ey?(0,_.jsx)(a.GX,{ref:e=>{j&&(j.current=e)},_dynamicHeights:es||F,_enableSectioningPosition:eu&&ep.includes("enabled_sections_position"),_getColumnSpanConfig:W,_getModulePositioningConfig:t,_getResponsiveModuleConfigForSecondItem:G,_logTwoColWhitespace:eM,_measureAll:eb,align:n,columnWidth:Y,gutterWidth:ee,items:eC,layout:U?J:b??"basic",loadItems:x,measurementStore:ea,minCols:et,renderItem:ek,scrollContainer:er,virtualBufferFactor:.3,virtualize:I}):(0,_.jsx)(a.Rk,{ref:e=>{j&&(j.current=e)},_dynamicHeights:es||F,_enableSectioningPosition:eu&&ep.includes("enabled_sections_position"),_getColumnSpanConfig:W,_getModulePositioningConfig:t,_getResponsiveModuleConfigForSecondItem:G,_logTwoColWhitespace:eM,align:n,columnWidth:Y,gutterWidth:ee,items:eC,layout:U?J:b??"basic",loadItems:x,measurementStore:ea,minCols:et,renderItem:ek,scrollContainer:er,virtualBufferFactor:.3,virtualize:I})})]})})]})}},587435:(e,t,n)=>{n.d(t,{Z:()=>i});function i(e=!0){let t=e?16:void 0,n=t?Math.floor(t/4):void 0;return{experimentalColumnWidth:e?221:void 0,experimentalGutter:t,experimentalGutterBoints:n}}},39260:(e,t,n)=>{n.d(t,{Z:()=>o});var i=n(297728),a=n(730212),r=n(855746);function o(e){let{isAuthenticated:t}=(0,a.B)(),{expName:n,anyEnabled:o,group:s}=function({experimentsClient:e,handlerId:t,isAuthenticated:n,skipActivation:i}){let{checkExperiment:a}=e,r=a(n?"web_masonry_v2_auth":"web_masonry_v2_unauth",{dangerouslySkipActivation:i});return r.group?{expName:n?"web_masonry_v2_auth":"web_masonry_v2_unauth",...r}:"www/[username]/[slug].js"!==t||n?"www/pin/[id].js"!==t||n?{expName:"",anyEnabled:!1,group:""}:{expName:"web_masonry_v2_unauth_pin",...a("web_masonry_v2_unauth_pin",{dangerouslySkipActivation:i})}:{expName:"web_masonry_v2_unauth_board",...a("web_masonry_v2_unauth_board",{dangerouslySkipActivation:i})}}({experimentsClient:(0,i.F)(),handlerId:(0,r.MM)(),isAuthenticated:t,skipActivation:e?.skipActivation??!1});return{expName:n,anyEnabled:o,group:s,isMeasureAllEnabled:"enabled_measure_all"===s||"enabled_measure_all_impression_fix"===s,isImpressionFixEnabled:"control_impression_fix"===s||"enabled_impression_fix"===s||"enabled_measure_all_impression_fix"===s}}}}]);
//# sourceMappingURL=https://sm.pinimg.com/webapp/16251-016b4c7cb4b32d9d.mjs.map