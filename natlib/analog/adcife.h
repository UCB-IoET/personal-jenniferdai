// This file is part of the Firestorm Software Distribution.
//
// FSD is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// FSD is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with FSD.  If not, see <http://www.gnu.org/licenses/>.

// Author: Michael Andersen <m.andersen@cs.berkeley.edu>

#ifndef _ADCIFE_H__
#define _ADCIFE_H__

typedef union
{
    uint32_t flat;
    struct
    {
        uint32_t swrst      : 1;
        uint32_t tstop      : 1;
        uint32_t tstart     : 1;
        uint32_t strig      : 1;
        uint32_t refbufen   : 1;
        uint32_t refbusdis  : 1;
        uint32_t _reserved0 : 2;
        uint32_t en         : 1;
        uint32_t dis        : 1;
        uint32_t bgreqen    : 1;
        uint32_t bgreqdis   : 1;
        uint32_t _reserved1 : 20;
    } __attribute__((__packed__)) bits;
} adcife_cr_t;

typedef union
{
    uint32_t flat;
    struct
    {
        uint32_t _reserved0 : 1;
        uint32_t refsel     : 3;
        uint32_t speed      : 2;
        uint32_t clksel     : 1;
        uint32_t _reserved1 : 1;
        uint32_t prescal    : 3;
        uint32_t _reserved2 : 21;
    } __attribute__((__packed__)) bits;
} adcife_cfg_t;

typedef union
{
    uint32_t flat;
    struct
    {
        uint32_t seoc       : 1;
        uint32_t lovr       : 1;
        uint32_t wm         : 1;
        uint32_t smtrg      : 1;
        uint32_t _reserved0 : 1;
        uint32_t tto        : 1;
        uint32_t _reserved1 : 18;
        uint32_t en         : 1;
        uint32_t tbusy      : 1;
        uint32_t sbusy      : 1;
        uint32_t cbusy      : 1;
        uint32_t refbuf     : 1;
        uint32_t _reserved2 : 1;
        uint32_t bgreq      : 1;
        uint32_t _reserved3 : 1;
    } __attribute__((__packed__)) bits;
} adcife_sr_t;

typedef union
{
    uint32_t flat;
    struct
    {
        uint32_t seoc       : 1;
        uint32_t lovr       : 1;
        uint32_t wm         : 1;
        uint32_t smtrg      : 1;
        uint32_t _reserved0 : 1;
        uint32_t tto        : 1;
        uint32_t _reserved1 : 26;
    } __attribute__((__packed__)) bits;
} adcife_scr_t;

typedef union
{
    uint32_t flat;
    struct
    {
        uint32_t hwla       : 1;
        uint32_t _reserved0 : 1;
        uint32_t bipolar    : 1;
        uint32_t _reserved1 : 1;
        uint32_t gain       : 3;
        uint32_t gcomp      : 1;
        uint32_t trgsel     : 3;
        uint32_t _reserved2 : 1;
        uint32_t res        : 1;
        uint32_t _reserved3 : 1;
        uint32_t internal   : 2;
        uint32_t muxpos     : 4;
        uint32_t muxneg     : 3;
        uint32_t _reserved4 : 5;
        uint32_t zoomrange  : 3;
        uint32_t _reserved5 : 1;
    } __attribute__((__packed__)) bits;
} adcife_seqcfg_t;

typedef union
{
    uint32_t flat;
    struct
    {
        uint32_t lcv        : 16;
        uint32_t lcpc       : 4;
        uint32_t lcnc       : 3;
        uint32_t _reserved0 : 9;
    } __attribute__((__packed__)) bits;
} adcife_lcv_t;

typedef struct
{
    adcife_cr_t     cr;     //off=0x0000
    adcife_cfg_t    cfg;    //off=0x0004
    adcife_sr_t     sr;     //off=0x0008
    adcife_scr_t    scr;    //off=0x000C
    uint32_t        _res0;
    adcife_seqcfg_t seqcfg; //off=0x0014
    uint32_t        cdma;   //off=0x0018
    uint32_t        tim;    //off=0x001C
    uint32_t        itimer; //off=0x0020
    uint32_t        wcfg;   //off=0x0024
    uint32_t        wth;    //off=0x0028
    adcife_lcv_t    lcv;    //off=0x002C
    uint32_t        ier;    //off=0x0030
    uint32_t        idr;    //off=0x0034
    uint32_t        imr;    //off=0x0038
    uint32_t        calib;  //off=0x003C
} adcife_t;

adcife_t volatile * const ADCIFE = (adcife_t volatile *) 0x40038000;

#endif
